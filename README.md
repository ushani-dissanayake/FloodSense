# FloodSense

Final-year project: **Smart Flood Detection and Early Warning System** for the Kalu Ganga basin.

ESP32 sensors publish water level, rainfall, temperature, and humidity to Firebase. A trained model scores flood risk. The Kotlin Android app shows live stations, alerts, and preparedness tools for citizens and administrators.

## What to submit

This folder is the project submission. Keep these parts together:

| Folder | What it is |
| --- | --- |
| `FloodSense-Kotlin-Android-Starter/FloodSense-Kotlin-Android/` | Native Android app (Kotlin + Jetpack Compose) |
| `FloodSense_ESP32/` | ESP32 firmware |
| `ml/` | Training scripts, FastAPI server, saved models |
| `Data_Set/` | Historical training CSV and field logs |

This folder is only the runnable project (app, firmware, ML, data). Mockups, thesis chapters, and unused spreadsheets are not included.

**Setup (Android, ESP32, ML API, Firebase, SMS):** see [SETUP.md](SETUP.md).
