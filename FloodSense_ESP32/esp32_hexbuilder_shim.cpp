// ESP32 Arduino core 3.0.x (including 3.0.7) often ships MD5Builder
// without linking HEXBuilder.cpp. Firebase_ESP_Client then fails with
// "undefined reference to HEXBuilder::hex2bytes / bytes2hex".
// Weak symbols fill the gap and are ignored if a later core already links HEXBuilder.

#include <Arduino.h>

#if defined(ESP_ARDUINO_VERSION_MAJOR) && (ESP_ARDUINO_VERSION_MAJOR >= 3)

#include <HEXBuilder.h>
#include <cctype>
#include <cstdlib>
#include <cstring>

size_t __attribute__((weak)) HEXBuilder::hex2bytes(unsigned char *out, size_t maxlen, const char *in) {
  while (in && *in && isspace(static_cast<unsigned char>(*in))) {
    in++;
  }
  const size_t inLen = in ? strlen(in) : 0;
  if (inLen == 0 || (inLen & 1) || (inLen > 2 * maxlen)) {
    return 0;
  }
  for (size_t i = 0; i < inLen; i += 2) {
    char tmp[3] = {in[i], in[i + 1], 0};
    char *err = nullptr;
    out[i / 2] = static_cast<unsigned char>(strtoul(tmp, &err, 16));
    if (err != nullptr && *err != '\0') {
      return 0;
    }
  }
  return inLen / 2;
}

size_t __attribute__((weak)) HEXBuilder::hex2bytes(unsigned char *out, size_t maxlen, const String &in) {
  return hex2bytes(out, maxlen, in.c_str());
}

size_t __attribute__((weak)) HEXBuilder::bytes2hex(char *out, size_t maxlen, const uint8_t *in, size_t len) {
  if (2 * len + 1 > maxlen) {
    return 0;
  }
  for (size_t i = 0; i < len; i++) {
    const uint8_t c = in[i];
    out[2 * i] = (c >> 4) + ((c >> 4) < 10 ? '0' : 'a' - 10);
    out[2 * i + 1] = (c & 0x0f) + ((c & 0x0f) < 10 ? '0' : 'a' - 10);
  }
  out[2 * len] = 0;
  return 2 * len;
}

String __attribute__((weak)) HEXBuilder::bytes2hex(const uint8_t *in, size_t len) {
  String out;
  out.reserve(2 * len + 1);
  for (size_t i = 0; i < len; i++) {
    const uint8_t c = in[i];
    out += char((c >> 4) + ((c >> 4) < 10 ? '0' : 'a' - 10));
    out += char((c & 0x0f) + ((c & 0x0f) < 10 ? '0' : 'a' - 10));
  }
  return out;
}

#endif
