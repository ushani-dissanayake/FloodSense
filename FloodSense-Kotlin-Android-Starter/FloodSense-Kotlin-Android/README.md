# FloodSense — Kotlin Android Studio Project

FloodSense is a native Android application for a university final-year project titled **Smart Flood Detection and Early Warning System**. Citizen and administrator screens are implemented with Kotlin and Jetpack Compose.

## Included in this project

### Shared
- Splash screen
- Role selection (Citizen or Administrator)
- Forgot-password screen
- Reusable FloodSense components and responsive Compose layouts
- MVVM-style ViewModel and repository structure
- Sri Lankan demonstration station, device, user, and alert data

### Citizen
- Login and registration
- Monitoring dashboard
- Station list with All / Online / Offline filters
- Station details with a custom water-level chart
- Flood-risk prediction screen
- Alerts list and alert details
- Seven-day history and statistics
- Schematic flood-risk map
- Preparedness Hub and readiness checklist
- User profile
- Settings, theme selector and notification preferences
- Emergency contacts

### Administrator
- Admin login with email and password
- Prediction dashboard (probability, contributing factors, district risk)
- Manage stations (search, filters, add station)
- Station details (thresholds, ESP32 health, send alert)
- Devices and sensors
- Manage alerts and create emergency alerts
- Manage users and add users
- Reports and analytics
- Admin settings

## Current operating mode

The supplied project runs in **demonstration mode**. It does not require Firebase, an ESP32, a Python server, or API keys to open and test the complete application flow.

### Citizen demo login

- Email: `student@floodsense.lk`
- Password: `123456`

The citizen login form also accepts any correctly formatted email and a password containing at least six characters. The Google sign-in button is a UI demonstration and enters the demo application without contacting Google.

### Administrator demo login

- Email: `admin@floodsense.lk`
- Password: `123456`

## Requirements

- Android Studio with JDK 17
- Android SDK 36 installed through Android Studio
- An Android emulator or physical device running Android 8.0 (API 26) or later
- Internet access during the first Gradle sync so Android Studio can download dependencies

## Open and run

1. Open Android Studio.
2. Select **Open** and choose the `FloodSense-Kotlin-Android` folder.
3. Wait for Gradle Sync to finish.
4. Accept any request to install Android SDK 36.
5. Create or select an Android emulator.
6. Press **Run ▶** and select the `app` configuration.
7. After splash, choose **Citizen monitoring** or **Administrator**.
8. Sign in using the matching demo credentials above.

## Main project folders

```text
app/src/main/java/com/floodsense/app/
├── MainActivity.kt
├── data/
│   ├── DemoData.kt
│   ├── FloodRepository.kt
│   └── Models.kt
├── navigation/
│   └── Screen.kt
└── ui/
    ├── FloodSenseApp.kt
    ├── FloodSenseViewModel.kt
    ├── components/
    ├── screens/
    └── theme/
```

## Navigation flow

```text
Splash → Role select
           ├── Citizen → Login / Register → Home
           │              ├── Stations → Station Details → Prediction / History
           │              ├── Map
           │              ├── Alerts → Alert Details
           │              ├── Preparedness Hub
           │              └── Profile → Settings and account pages
           └── Admin → Admin Login → Dashboard
                          ├── Stations → Add Station / Station Details
                          ├── Alerts → Create Emergency Alert
                          ├── Users → Add User
                          └── More → Devices / Reports / Settings
```

## Live system

`FirebaseFloodRepository` reads ESP32 readings from Firebase Realtime Database. Optional extras:

1. FastAPI model server (`ml/serve_api.py`) — set `ml.api.base` in `local.properties`
2. text.lk SMS — set `textlk.api.token` in `local.properties` (do not submit a real token). At most 2 SMS are sent for the demo.
3. Firebase Cloud Functions in `functions/` for push alerts

Copy `local.properties.example` to `local.properties` if Android Studio has not created that file yet.

Full environment setup (ESP32, FastAPI, SMS, Firebase) is in the repo root **[SETUP.md](../../SETUP.md)**.

The app still opens in demonstration mode if Firebase or hardware is unavailable.
