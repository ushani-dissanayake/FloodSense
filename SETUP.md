# FloodSense project setup

Step-by-step setup for the Android app, ESP32 node, machine-learning API, Firebase, and optional SMS. Work from the `FloodSense` folder unless a step says otherwise.

```text
FloodSense/
├── FloodSense-Kotlin-Android-Starter/FloodSense-Kotlin-Android/   Android app
├── FloodSense_ESP32/                                              ESP32 firmware
├── ml/                                                            Training + FastAPI
├── Data_Set/                                                      Training CSV and field logs
├── README.md
└── SETUP.md                                                       This file
```

The app can open in demonstration mode without hardware. Live water level, rainfall, siren, and Critical SMS need Firebase plus (for sensors) an ESP32.

---

## 1. What you need

| Piece | Requirement |
| --- | --- |
| Computer | macOS, Windows, or Linux |
| Android | Android Studio (Giraffe or newer), **JDK 17**, Android SDK **34** |
| Device | Emulator or phone on **Android 8.0 (API 26)** or later |
| ESP32 | Arduino IDE 2.x, ESP32 board package, USB cable |
| Python | **3.10+** (only for retraining or the Random Forest API) |
| Node.js | **20** (only if you deploy Cloud Functions) |
| Network | Internet for the first Gradle sync, Firebase, and text.lk |

Firebase project used by this checkout: **floodsense-3b160**  
Realtime Database: `https://floodsense-3b160-default-rtdb.asia-southeast1.firebasedatabase.app`

---

## 2. Android app

### 2.1 Open the project

1. Start **Android Studio**.
2. **Open** this folder (not the outer `FloodSense` folder):

   `FloodSense-Kotlin-Android-Starter/FloodSense-Kotlin-Android`

3. Wait for Gradle Sync. Accept SDK / JDK prompts if Android Studio asks.
4. Confirm **JDK 17**: *Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK*.

### 2.2 `local.properties`

Android Studio usually creates this file with `sdk.dir`. If it is missing, copy `local.properties.example` and set your SDK path.

```properties
sdk.dir=/Users/YOUR_NAME/Library/Android/sdk

# Optional. Emulator → laptop FastAPI. On a real phone use your laptop LAN IP.
ml.api.base=http://10.0.2.2:8000

# Optional. text.lk SMS for Critical / ISSUE_ALERT only. At most 2 demo messages.
textlk.api.token=
textlk.sender.id=TextLKDemo
```

- **Do not zip or commit** `local.properties` (it is machine-specific and can hold the SMS token).
- After changing the token or `ml.api.base`, do a **Rebuild Project** so `BuildConfig` updates.

Windows SDK example: `sdk.dir=C\:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk`

### 2.3 Firebase file

Keep `app/google-services.json` in place (`package_name` must stay `com.floodsense.app`). Without it, Gradle sync with the Google Services plugin fails.

### 2.4 Run

1. Select the **app** run configuration.
2. Pick an emulator or a USB-debuggable phone.
3. Press **Run**.
4. After splash, choose **Citizen** or **Administrator**.

Demo accounts (also work if Firebase is unreachable):

| Role | Email | Password |
| --- | --- | --- |
| Citizen | `student@floodsense.lk` | `123456` |
| Admin | `admin@floodsense.lk` | `123456` |

Citizen registration also accepts any valid email and a password of at least six characters. Google sign-in is a UI demo and does not call Google.

### 2.5 Live vs demo

- **Demo:** stations and alerts from bundled data; you can walk every screen without ESP32.
- **Live:** `FirebaseFloodRepository` listens to `FloodSystem/stations/<station-id>`. Home stations are only the Kalu Ganga gauges in `Data_Set/Kalu_Ganga_1000_Records.xlsx`.

Default Home station: **Kalu Ganga - Putupaula** (`kalu-millaniya`).

---

## 3. ESP32 firmware

### 3.1 Arduino IDE

1. Install [Arduino IDE 2.x](https://www.arduino.cc/en/software).
2. *File → Preferences → Additional boards manager URLs:*

   `https://espressif.github.io/arduino-esp32/package_esp32_index.json`

3. *Tools → Board → Boards Manager* → install **esp32** by Espressif (3.0.x is fine).
4. *Tools → Manage Libraries* → install **Firebase Arduino Client Library for ESP8266 and ESP32** (Mobizt).

`esp32_hexbuilder_shim.cpp` must stay next to `FloodSense_ESP32.ino`. It fixes a HEXBuilder linker gap on ESP32 core 3.0.x. Do not move or rename it.

### 3.2 Board settings

| Setting | Value |
| --- | --- |
| Board | ESP32 Dev Module (or your module) |
| Upload speed | 115200 |
| Serial Monitor | **115200** baud |

### 3.3 Edit before upload

Open `FloodSense_ESP32/FloodSense_ESP32.ino` and set:

```cpp
#define WIFI_SSID "YourWifiName"
#define WIFI_PASSWORD "YourWifiPassword"
#define STATION_ID "kalu-millaniya"
#define STATION_NAME "Kalu Ganga - Putupaula"
```

`STATION_ID` must match an app station **exactly**:

| `STATION_ID` | App station | District |
| --- | --- | --- |
| `kalu-millaniya` | Kalu Ganga - Putupaula (default) | Kalutara |
| `kalawellawa` | Kalawellawa | Kalutara |
| `ellagawa` | Ellagawa | Ratnapura |
| `rathnapura` | Rathnapura | Ratnapura |
| `magura` | Magura | Ratnapura |

Leave `STATION_ID` as `kalu-millaniya` unless you want the board to drive a different Home station. Serial should print `kalu-millaniya` after boot. If it still prints `kelani-seethawaka`, the new sketch was not uploaded.

Firebase URL and API key in the sketch must match `app/google-services.json` (same project).

### 3.4 Wiring

| Function | GPIO |
| --- | --- |
| Analog water probe | **34** (power pin **32**) |
| Rain plate | **35** |
| Ultrasonic TRIG / ECHO | **4** / **5** |
| Siren | **26** |
| Battery ADC | **33** |
| Solar ADC | **36** |

Water probe is a **250 ml** demo beaker:

| Beaker | Mapped level | App / siren |
| --- | --- | --- |
| Empty | 0.00 m | Low |
| **200 ml** | **8.00 m** | HIGH + local siren |
| 250 ml | 10.00 m | Full scale |

### 3.5 Upload and calibrate

1. Select the serial port and upload the sketch.
2. Open Serial Monitor at 115200.
3. Confirm Wi-Fi, then `Firebase Connection Successful!`
4. Note the `water=` ADC values: dry (empty) and filled to 250 ml.
5. Put those numbers into `WATER_ADC_EMPTY` and `WATER_ADC_FULL`, then upload again.
6. Rain plate: dry should be near `RAIN_ADC_DRY` (this board uses **4095** dry, lower when wet).

Live fields land at `FloodSystem/stations/kalu-millaniya/` (`water_level`, `rainfall`, `flood_risk`, `last_seen`, `siren_command`, `siren_mute`).

---

## 4. Machine-learning API (optional)

The app **always** scores risk on-device with the exported decision tree (`FloodRiskEngine`: high water **8.0 m**, high rain **200 mm**). That is enough for the FYP demo.

The Python server adds the saved **Random Forest** on top. If it is down, Logcat shows `FloodMl: RF API unavailable` and the on-device tree keeps working.

### 4.1 Install and serve

From the `FloodSense` folder:

```bash
python3 -m pip install -r ml/requirements.txt
python3 ml/serve_api.py
```

Listens on **http://0.0.0.0:8000**.

| Client | `ml.api.base` in `local.properties` |
| --- | --- |
| Android emulator | `http://10.0.2.2:8000` |
| Physical phone | `http://YOUR_LAPTOP_LAN_IP:8000` (same Wi-Fi; allow port 8000 on the firewall) |

Check: open `http://127.0.0.1:8000/health` on the laptop.

### 4.2 Retrain (optional)

Models in `ml/models/` are already trained from `Data_Set/flood_data.csv`. To rebuild:

```bash
python3 ml/train_flood_model.py
```

Writes `ml/models/*.joblib`, `ml/models/flood_model.json`, and copies the JSON to `app/src/main/assets/flood_model.json`. Field-gauge check:

```bash
python3 ml/validate_field_logs.py
```

---

## 5. Firebase (live path)

Already wired for project **floodsense-3b160**.

1. [Firebase Console](https://console.firebase.google.com/) → Authentication → Email/Password enabled.
2. Realtime Database in **asia-southeast1**, with rules that allow the app and ESP32 anonymous signup to write `FloodSystem/` (and authenticated users to write `users/$uid`).
3. Cloud Messaging enabled if you want FCM topic `floodsense_citizens`.

Useful paths:

| Path | Role |
| --- | --- |
| `FloodSystem/stations/<id>` | Live ESP32 readings and siren flags |
| `FloodSystem/issue_alert` | Critical / ISSUE_ALERT payload |
| `FloodSystem/sms_recipients/<uid>` | Emergency numbers for SMS |
| `FloodSystem/sms_config` | `sends_used`, optional `api_token`, `via_function` |
| `users/<uid>/emergency_contacts` | Contacts saved in Profile |

---

## 6. Emergency SMS (optional)

SMS is sent **only** for **Critical** admin alerts and automatic **ISSUE_ALERT** (HIGH water). Cap: **2 messages** for the text.lk free quota.

1. Create a [text.lk](https://text.lk/) account. Use sender id **TextLKDemo** on the free/demo plan.
2. Put the API token in `local.properties` as `textlk.api.token=` (never commit it).
3. **Rebuild** the app.
4. Sign in as **citizen** → Profile → Emergency contacts → save a real `07XXXXXXXX` number (save again even if it was already there).
5. Sign in as **admin** → Create Emergency Alert → **CRITICAL** → send.

Logcat filters: `EmergencySms`, `TextLkSms`.

If Logcat says quota exhausted, set `FloodSystem/sms_config/sends_used` to `0` in Firebase and clear the app’s storage.

Cloud Functions (`functions/`) can also send FCM (and SMS only if `FloodSystem/sms_config/via_function` is `true`). Default: the **app** sends SMS so you do not need to deploy functions for the demo.

To deploy functions (optional):

```bash
cd FloodSense-Kotlin-Android-Starter/FloodSense-Kotlin-Android
npm --prefix functions install
# copy functions/.env.example → functions/.env if the function should call text.lk
firebase deploy --only functions
```

---

## 7. End-to-end demo checklist

1. Android app runs; citizen and admin demo logins work.
2. ESP32 Serial: Wi-Fi + Firebase OK, `STATION_ID` is `kalu-millaniya`.
3. App Home → Kalutara → Putupaula shows live water / rain updating about every 5 seconds.
4. Fill the beaker to **200 ml** → risk HIGH, GPIO 26 siren, citizen ISSUE_ALERT.
5. (Optional) FastAPI `/health` OK; Prediction screen source may show Random Forest instead of On-device tree.
6. (Optional) Critical alert + saved `07` contact → SMS (max 2).

---

## 8. Common problems

| Symptom | What to check |
| --- | --- |
| Gradle sync fails | JDK 17, SDK 34, internet, `google-services.json` present |
| App opens but no live water | ESP32 `STATION_ID` must be `kalu-millaniya` (or the station you selected on Home) |
| Always HIGH rain when dry | Rain ADC: dry is high (~4095) on this board; re-upload current firmware |
| Beaker never reaches HIGH | Recalibrate `WATER_ADC_EMPTY` / `WATER_ADC_FULL` from Serial `water=` |
| `undefined reference to HEXBuilder` | `esp32_hexbuilder_shim.cpp` missing from the sketch folder |
| RF API unavailable | Start `python3 ml/serve_api.py`; on a phone set `ml.api.base` to the laptop LAN IP |
| Critical alert, no SMS | Rebuild after token; re-save a `07` contact; quota `sends_used` < 2 |
| Emulator cannot reach FastAPI | Use `10.0.2.2`, not `127.0.0.1` |
| `./gradlew` hangs on macOS | Run from Android Studio instead |

---

## 9. What not to submit

Do not include in a zip or git push:

- `local.properties` (SDK path + SMS token)
- `functions/.env`
- `app/build/`, `.gradle/`, `.idea/`
- Real text.lk tokens

Keep `local.properties.example` and `functions/.env.example` as templates.
