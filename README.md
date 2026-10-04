# 🌊 FloodSense

### Intelligent IoT-Based Flood Detection & Early Warning System

FloodSense is a final-year Software Engineering project designed to provide real-time flood monitoring and early warning by integrating **IoT sensing, cloud technologies, machine learning, and Android application development**.

The prototype focuses on flood monitoring within the **Kalu Ganga basin**, using an ESP32-based sensing unit to collect environmental observations and provide real-time flood-risk information to users.

---

## 🚀 Key Features

- 🌊 Real-time water-level monitoring
- 🌧️ Real-time rainfall monitoring
- 📡 ESP32-based environmental data collection
- ☁️ Firebase Realtime Database integration
- 🤖 Machine-learning-based flood risk classification
- 🟢 LOW, 🟠 MEDIUM and 🔴 HIGH flood-risk levels
- 📱  Native Android application for real-time monitoring
- 🔔 Automatic warning notifications
- 🚨 Local siren activation during HIGH-risk conditions
- 🛡️ Fallback risk classification when the external ML service is unavailable
- 👤 Separate citizen and administrator functionality
- 📊 Flood monitoring and alert information through the mobile application

---

## 🏗️ System Architecture

FloodSense follows a layered architecture:

**IoT Sensors → ESP32 → Firebase Realtime Database → ML Prediction → Android Application → Warning & Alert System**

### 1. IoT / Edge Layer
The ESP32 collects environmental sensor readings including water level and rainfall and sends the observations to the cloud.

### 2. Cloud & Data Layer
Firebase Realtime Database is used to synchronize real-time monitoring information between system components.

### 3. Machine Learning Layer
A Python-based ML service uses flood-related observations to classify flood risk as:

- **LOW**
- **MEDIUM**
- **HIGH**

Random Forest is used for the main flood-risk classification, while additional decision and fallback mechanisms improve system reliability.

### 4. Application Layer
A native Android application developed using **Kotlin and Jetpack Compose** provides real-time monitoring, predictions, alerts and role-based functionality.

### 5. Warning Layer
HIGH-risk conditions can trigger automatic warning notifications and local siren activation.

---

## 🛠️ Technologies Used

### IoT & Embedded
`ESP32` `Arduino` `C++` `IoT Sensors`

### Android
`Kotlin` `Jetpack Compose` `Android Studio`

### Cloud
`Firebase Realtime Database` `Firebase Cloud Messaging`

### Machine Learning & API
`Python` `Scikit-learn` `Random Forest` `Decision Tree` `FastAPI` `Uvicorn`

### Data Processing
`Pandas` `NumPy`

### Development Tools
`Git` `GitHub` `VS Code` `Android Studio`

---

## 📂 Project Structure

```text
FloodSense/
│
├── Data_Set/
│   └── Flood-related datasets and field logs
│
├── FloodSense-Kotlin-Android-Starter/
│   └── Native Android application
│
├── FloodSense_ESP32/
│   └── ESP32 firmware and sensor integration
│
├── ml/
│   ├── Model training
│   ├── Flood-risk prediction
│   ├── FastAPI service
│   └── Saved ML models
│
├── README.md
├── SETUP.md
└── .gitignore
