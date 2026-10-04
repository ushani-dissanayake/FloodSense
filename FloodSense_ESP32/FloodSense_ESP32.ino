#include <WiFi.h>
#include <time.h>
#include <string.h>
#include <Firebase_ESP_Client.h>

#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// HEXBuilder is supplied by esp32_hexbuilder_shim.cpp (ESP32 core 3.0.7 linker gap).

#include "secrets.h"

#define RAIN_SENSOR 35
#define WATER_SENSOR 34
#define WATER_POWER 32
#define TRIG_PIN 4
#define ECHO_PIN 5
#define SIREN_PIN 26
#define BATTERY_ADC 33
#define SOLAR_ADC 36
#define SOLAR_ADC_ENABLED 1

// Battery/solar through a 220k (from pack+) / 100k (to GND) divider.
// Midpoint goes to the ADC pin. Never put the pack on a GPIO.
// Max measurable voltage is about 10.6 V. Tie GPIO 36 to GND if unused.
#define DIVIDER_RHIGH 220000.0
#define DIVIDER_RLOW 100000.0
#define ADC_VREF 3.30
#define VOLTAGE_MIN_VALID 1.80
#define VOLTAGE_MAX_VALID 12.00

// Change STATION_ID on each ESP32. Must match the FloodSense Home station:
//   kalu-millaniya      Kalu Ganga - Putupaula (Kalutara, default Home)
//   kalawellawa         Kalu Ganga - Kalawellawa
//   ellagawa            Kalu Ganga - Ellagawa (Ratnapura)
//   rathnapura          Kalu Ganga - Rathnapura
//   magura              Maguru Ganga - Magura
#define STATION_ID "kalu-millaniya"
#define STATION_NAME "Kalu Ganga - Putupaula"

// 250 ml demo beaker on the analog water probe (GPIO 34):
//   empty     → 0 ml  → 0.00 m
//   200 ml    → HIGH  → 8.00 m  + local siren
//   250 ml    → full  → 10.00 m
// Calibrate WATER_ADC_EMPTY (dry) and WATER_ADC_FULL (filled to 250 ml)
// from the Serial "water=" ADC reading.
#define WATER_ADC_EMPTY 0
#define WATER_ADC_FULL 3200
#define WATER_LEVEL_MAX_M 10.0
#define BEAKER_CAPACITY_ML 250.0
#define BEAKER_HIGH_ML 200.0

// Dry rain plate on this board reads ADC 4095. Wet reads lower.
// Mapping 0→dry was wrong: a dry 4095 became 250 mm and HIGH risk.

#define RAIN_ADC_DRY 4095
#define RAIN_ADC_WET 800
#define RAIN_ADC_DRY_FLOOR 3200
#define RAINFALL_MAX_MM 250.0

#define SAMPLE_INTERVAL_MS 5000
#define SIREN_POLL_MS 400
#define WIFI_WAIT_MS 12000
#define QUEUE_CAP 72

struct Sample {
  unsigned long unixSec;
  float waterLevelM;
  float rainfallMm;
  float ultrasonicCm;
  float batteryV;
  float solarV;
  int floodRisk;
  int waterAdc;
  int raining;
  bool ultraOk;
  bool batteryOk;
  bool solarOk;
  bool sirenActive;
};

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;
bool signupOK = false;
Sample offlineQueue[QUEUE_CAP];
int queueHead = 0;
int queueCount = 0;
unsigned long lastWifiAttemptMs = 0;
unsigned long lastFirebaseAttemptMs = 0;
bool remoteSirenCommand = false;
bool remoteSirenMute = false;
bool haveRemoteSiren = false;
bool sirenActive = false;

void enqueueSample(const Sample& sample);
bool flushQueue();
bool publishLive(const Sample& sample);
bool publishLiveTo(const char* stationId, const Sample& sample);
bool publishHistory(const Sample& sample);
bool firebaseUp();
void ensureWifi();
void ensureFirebase();
void beginFirebase();
unsigned long unixNow();
float readDividerVolts(int pin);
bool voltageOk(float volts);
void logRtdbError(const char* what, const char* path);
float rainfallFromAdc(int rainAdc);
void applySiren(int floodRisk);
void pollRemoteSiren();
void waitWithSiren(int floodRisk);

float mapFloat(float x, float inMin, float inMax, float outMin, float outMax) {
  if (inMax == inMin) {
    return outMin;
  }
  float y = (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
  float lo = outMin < outMax ? outMin : outMax;
  float hi = outMin < outMax ? outMax : outMin;
  if (y < lo) y = lo;
  if (y > hi) y = hi;
  return y;
}

int analogAverage(int pin, int samples = 16) {
  uint32_t sum = 0;
  for (int i = 0; i < samples; i++) {
    sum += analogRead(pin);
    delay(2);
  }
  return (int)(sum / samples);
}

float readUltrasonicCm() {
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);
  long duration = pulseIn(ECHO_PIN, HIGH, 30000);
  if (duration <= 0) {
    return -1.0;
  }
  return duration * 0.0343 / 2.0;
}

float readDividerVolts(int pin) {
  uint32_t sum = 0;
  for (int i = 0; i < 8; i++) {
    sum += analogRead(pin);
    delay(2);
  }
  float vAdc = (sum / 8.0) * ADC_VREF / 4095.0;
  return vAdc * (DIVIDER_RHIGH + DIVIDER_RLOW) / DIVIDER_RLOW;
}

bool voltageOk(float volts) {
  return volts >= VOLTAGE_MIN_VALID && volts <= VOLTAGE_MAX_VALID;
}

float rainfallFromAdc(int rainRaw) {
  if (rainRaw >= RAIN_ADC_DRY_FLOOR || rainRaw < 50) {
    return 0.0;
  }
  return mapFloat(rainRaw, RAIN_ADC_DRY, RAIN_ADC_WET, 0.0, RAINFALL_MAX_MM);
}

// Same tree as FloodRiskEngine. HIGH water = 200 ml in the 250 ml beaker
// (mapped to 8.0 m on the 0–10 m probe scale the app expects).
int classifyFloodRisk(float waterLevelM, float rainfallMm, float waterMl) {
  if (rainfallMm > 200.0) return 2;
  if (waterMl > BEAKER_HIGH_ML) return 2;
  if (rainfallMm > 59.97) return 1;
  if (waterLevelM > 4.97) return 1;
  return 0;
}

void applySiren(int floodRisk) {
  bool localHigh = floodRisk >= 2;
  if (haveRemoteSiren) {
    sirenActive = remoteSirenCommand || (localHigh && !remoteSirenMute);
  } else {
    sirenActive = localHigh;
  }
  digitalWrite(SIREN_PIN, sirenActive ? HIGH : LOW);
}

void pollRemoteSiren() {
  if (!firebaseUp()) {
    return;
  }
  char path[96];
  snprintf(path, sizeof(path), "/FloodSystem/stations/%s/siren_command", STATION_ID);
  if (Firebase.RTDB.getBool(&fbdo, path)) {
    remoteSirenCommand = fbdo.boolData();
    haveRemoteSiren = true;
  }
  snprintf(path, sizeof(path), "/FloodSystem/stations/%s/siren_mute", STATION_ID);
  if (Firebase.RTDB.getBool(&fbdo, path)) {
    remoteSirenMute = fbdo.boolData();
  }
}

void waitWithSiren(int floodRisk) {
  unsigned long start = millis();
  while (millis() - start < SAMPLE_INTERVAL_MS) {
    pollRemoteSiren();
    applySiren(floodRisk);
    delay(SIREN_POLL_MS);
  }
}

void setup() {
  Serial.begin(115200);
  delay(300);
  Serial.println();
  Serial.println("=== FloodSense node: kalu-millaniya ===");
  Serial.println("Beaker 250 ml full; 200 ml = HIGH risk + siren on GPIO 26.");
  Serial.println("If you still see kelani-seethawaka, this file was not uploaded.");

  pinMode(WATER_POWER, OUTPUT);
  digitalWrite(WATER_POWER, LOW);
  pinMode(WATER_SENSOR, INPUT);
  pinMode(RAIN_SENSOR, INPUT);

  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  digitalWrite(TRIG_PIN, LOW);
  pinMode(SIREN_PIN, OUTPUT);
  digitalWrite(SIREN_PIN, LOW);
  analogSetAttenuation(ADC_11db);
  analogReadResolution(12);
  pinMode(BATTERY_ADC, INPUT_PULLDOWN);

  WiFi.mode(WIFI_STA);
  ensureWifi();
  if (WiFi.status() == WL_CONNECTED) {
    configTime(0, 0, "pool.ntp.org", "time.google.com");
    Serial.print("Syncing time");
    for (int i = 0; i < 20 && time(nullptr) < 1700000000; i++) {
      Serial.print(".");
      delay(250);
    }
    Serial.println();
  } else {
    Serial.println("WiFi not ready. Sampling continues and readings stay in the offline queue.");
  }

  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  Firebase.reconnectWiFi(true);
  Firebase.begin(&config, &auth);
  beginFirebase();
}

void loop() {
  int rainRaw = analogAverage(RAIN_SENSOR);
  float rainfallMm = rainfallFromAdc(rainRaw);
  int rainShown = rainRaw >= RAIN_ADC_DRY_FLOOR ? 0 : rainRaw;

  digitalWrite(WATER_POWER, HIGH);
  delay(80);
  int waterAdc = analogAverage(WATER_SENSOR);
  digitalWrite(WATER_POWER, LOW);
  float waterMl = mapFloat(waterAdc, WATER_ADC_EMPTY, WATER_ADC_FULL, 0.0, BEAKER_CAPACITY_ML);
  float waterLevelM = (waterMl / BEAKER_CAPACITY_ML) * WATER_LEVEL_MAX_M;

  float ultrasonicCm = readUltrasonicCm();
  bool ultraOk = ultrasonicCm > 0.0 && ultrasonicCm < 400.0;

  float batteryV = readDividerVolts(BATTERY_ADC);
  bool batteryOk = voltageOk(batteryV);
#if SOLAR_ADC_ENABLED
  float solarV = readDividerVolts(SOLAR_ADC);
  bool solarOk = voltageOk(solarV);
#else
  float solarV = 0.0;
  bool solarOk = false;
#endif

  int floodRisk = classifyFloodRisk(waterLevelM, rainfallMm, waterMl);
  applySiren(floodRisk);

  Serial.print("ADC rain=");
  Serial.print(rainShown);
  Serial.print(" water=");
  Serial.print(waterAdc);
  Serial.print("  |  rainfall_mm=");
  Serial.print(rainfallMm, 1);
  Serial.print(" water_m=");
  Serial.print(waterLevelM, 2);
  Serial.print(" beaker_ml=");
  Serial.print(waterMl, 0);
  Serial.print(" risk=");
  Serial.print(floodRisk);
  Serial.print(" siren=");
  Serial.print(sirenActive ? "ON" : "off");
  if (ultraOk) {
    Serial.print(" ultrasonic_cm=");
    Serial.print(ultrasonicCm, 1);
  } else {
    Serial.print(" ultrasonic read failed");
  }
  if (batteryOk) {
    Serial.print(" battery_v=");
    Serial.print(batteryV, 2);
  } else {
    Serial.print(" battery not connected");
  }
  if (solarOk) {
    Serial.print(" solar_v=");
    Serial.print(solarV, 2);
  } else {
    Serial.print(" solar not connected");
  }
  Serial.println();
  static bool hintedDrySensors = false;
  if (!hintedDrySensors && waterAdc <= 5 && rainRaw >= 4000) {
    hintedDrySensors = true;
    Serial.println("Dry/unwired ADC: wet GPIO 34 (water) or GPIO 35 (rain). water_m stays 0 until WATER ADC rises.");
  }

  Sample sample;
  sample.unixSec = unixNow();
  sample.waterLevelM = waterLevelM;
  sample.rainfallMm = rainfallMm;
  sample.ultrasonicCm = ultrasonicCm;
  sample.floodRisk = floodRisk;
  sample.waterAdc = waterAdc;
  sample.raining = rainfallMm > 20.0 ? 1 : 0;
  sample.ultraOk = ultraOk;
  sample.batteryV = batteryV;
  sample.solarV = solarV;
  sample.batteryOk = batteryOk;
  sample.solarOk = solarOk;
  sample.sirenActive = sirenActive;

  ensureWifi();
  ensureFirebase();

  if (firebaseUp()) {
    int flushed = queueCount;
    if (!flushQueue()) {
      Serial.printf("Queue flush failed, holding %d samples\n", queueCount);
    } else if (flushed > 0) {
      Serial.printf("Flushed %d queued samples\n", flushed);
    }
    if (!publishLive(sample)) {
      enqueueSample(sample);
      Serial.printf("Live publish failed, queued %d/%d\n", queueCount, QUEUE_CAP);
    } else {
      Serial.print("Live node updated  FloodSystem/stations/");
      Serial.print(STATION_ID);
      Serial.print("  last_seen=");
      Serial.println(sample.unixSec);
      if (!publishHistory(sample)) {
        Serial.printf("History write failed: %s\n", fbdo.errorReason().c_str());
      }
    }
  } else {
    enqueueSample(sample);
    Serial.printf("Offline, queued %d/%d\n", queueCount, QUEUE_CAP);
  }

  Serial.println("-------------------------");
  waitWithSiren(floodRisk);
}

void enqueueSample(const Sample& sample) {
  if (queueCount == QUEUE_CAP) {
    queueHead = (queueHead + 1) % QUEUE_CAP;
    queueCount--;
  }
  offlineQueue[(queueHead + queueCount) % QUEUE_CAP] = sample;
  queueCount++;
}

bool flushQueue() {
  while (queueCount > 0) {
    if (!publishHistory(offlineQueue[queueHead])) {
      return false;
    }
    queueHead = (queueHead + 1) % QUEUE_CAP;
    queueCount--;
  }
  return true;
}

bool publishLive(const Sample& sample) {
  return publishLiveTo(STATION_ID, sample);
}

bool publishLiveTo(const char* stationId, const Sample& sample) {
  char path[96];
  snprintf(path, sizeof(path), "/FloodSystem/stations/%s", stationId);
  FirebaseJson json;
  json.set("water_level", (double)sample.waterLevelM);
  json.set("rainfall", (double)sample.rainfallMm);
  json.set("flood_risk", sample.floodRisk);
  json.set("name", STATION_NAME);
  json.set("queue_depth", queueCount);
  json.set("last_seen", (double)sample.unixSec);
  json.set("ultrasonic_distance", sample.ultraOk ? (double)sample.ultrasonicCm : 0.0);
  json.set("siren_active", sample.sirenActive);
  if (sample.batteryOk) {
    json.set("battery_voltage", (double)sample.batteryV);
  }
  if (sample.solarOk) {
    json.set("solar_voltage", (double)sample.solarV);
  }
  if (!Firebase.RTDB.updateNode(&fbdo, path, &json)) {
    logRtdbError("updateNode", path);
    bool waterOk = Firebase.RTDB.setFloat(&fbdo, String(path) + "/water_level", sample.waterLevelM);
    bool rainOk = Firebase.RTDB.setFloat(&fbdo, String(path) + "/rainfall", sample.rainfallMm);
    bool riskOk = Firebase.RTDB.setInt(&fbdo, String(path) + "/flood_risk", sample.floodRisk);
    bool seenOk = Firebase.RTDB.setDouble(&fbdo, String(path) + "/last_seen", (double)sample.unixSec);
    if (!waterOk) logRtdbError("set water_level", path);
    if (!rainOk) logRtdbError("set rainfall", path);
    if (!riskOk) logRtdbError("set flood_risk", path);
    if (!seenOk) logRtdbError("set last_seen", path);
    return waterOk && rainOk && seenOk;
  }
  return true;
}

bool publishHistory(const Sample& sample) {
  FirebaseJson json;
  json.set("timestamp", (double)sample.unixSec);
  json.set("water_level", sample.waterLevelM);
  json.set("rainfall", sample.rainfallMm);
  json.set("flood_risk", sample.floodRisk);
  json.set("WaterLevel", sample.waterAdc);
  json.set("RainStatus", sample.raining);
  if (sample.ultraOk) {
    json.set("ultrasonic_distance", sample.ultrasonicCm);
  }
  if (sample.batteryOk) {
    json.set("battery_voltage", sample.batteryV);
  }
  if (sample.solarOk) {
    json.set("solar_voltage", sample.solarV);
  }
  char path[96];
  if (sample.unixSec > 1700000000UL) {
    snprintf(path, sizeof(path), "/FloodSystem/stations/%s/history/%lu", STATION_ID, sample.unixSec);
    return Firebase.RTDB.setJSON(&fbdo, path, &json);
  }
  snprintf(path, sizeof(path), "/FloodSystem/stations/%s/history", STATION_ID);
  return Firebase.RTDB.pushJSON(&fbdo, path, &json);
}

bool firebaseUp() {
  return WiFi.status() == WL_CONNECTED && signupOK && Firebase.ready();
}

void ensureWifi() {
  if (WiFi.status() == WL_CONNECTED) {
    return;
  }
  unsigned long now = millis();
  if (lastWifiAttemptMs != 0 && now - lastWifiAttemptMs < 8000) {
    return;
  }
  lastWifiAttemptMs = now;
  Serial.print("Connecting WiFi");
  WiFi.disconnect();
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - start < WIFI_WAIT_MS) {
    delay(400);
    Serial.print(".");
  }
  Serial.println(WiFi.status() == WL_CONNECTED ? "\nWiFi Connected!" : "\nWiFi failed, will retry");
}

void beginFirebase() {
  if (signupOK || WiFi.status() != WL_CONNECTED) {
    return;
  }
  unsigned long now = millis();
  if (lastFirebaseAttemptMs != 0 && now - lastFirebaseAttemptMs < 10000) {
    return;
  }
  lastFirebaseAttemptMs = now;
  if (!Firebase.signUp(&config, &auth, "", "")) {
    Serial.printf("Firebase signup: %s\n", config.signer.signupError.message.c_str());
    return;
  }
  signupOK = true;
  Serial.println("Firebase Connection Successful!");
  Serial.print("Writing live node /FloodSystem/stations/");
  Serial.println(STATION_ID);
  Serial.print("Waiting token");
  for (int i = 0; i < 40 && !Firebase.ready(); i++) {
    Serial.print(".");
    delay(250);
  }
  Serial.println(Firebase.ready() ? " ready" : " NOT ready");
}

void ensureFirebase() {
  if (signupOK) {
    return;
  }
  beginFirebase();
}

void logRtdbError(const char* what, const char* path) {
  Serial.printf("RTDB %s failed (%s): %s\n", what, path, fbdo.errorReason().c_str());
}

unsigned long unixNow() {
  time_t now = time(nullptr);
  if (now > 1700000000) {
    return (unsigned long)now;
  }
  return millis() / 1000UL;
}
