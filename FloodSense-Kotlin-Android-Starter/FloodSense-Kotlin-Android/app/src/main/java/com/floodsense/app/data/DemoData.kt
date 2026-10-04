package com.floodsense.app.data

object DemoData {
    val stations = listOf(
        Station(
            id = "kelani-seethawaka",
            name = "Kelani River - Seethawaka",
            district = "Colombo District",
            waterLevel = 7.35,
            rainfall = 128.4,
            ultrasonicDistance = 42.5,
            online = true,
            riskLevel = RiskLevel.HIGH,
            latitude = 6.9553,
            longitude = 80.2147,
            chartValues = listOf(2.1f, 2.4f, 2.8f, 3.3f, 3.7f, 4.1f, 4.4f, 4.8f, 5.1f, 5.4f, 5.8f, 6.1f, 6.4f, 6.7f, 6.9f, 7.05f, 7.18f, 7.22f, 7.28f, 7.35f),
            deviceId = "ESP32-007"
        ),
        Station(
            id = "kalu-millaniya",
            name = "Kalu Ganga - Putupaula",
            district = "Kalutara District",
            waterLevel = 5.20,
            rainfall = 71.2,
            ultrasonicDistance = 77.3,
            online = true,
            riskLevel = RiskLevel.MEDIUM,
            latitude = 6.6182,
            longitude = 80.0047,
            chartValues = listOf(2.4f, 2.7f, 3.1f, 3.5f, 3.8f, 4.1f, 4.4f, 4.7f, 4.9f, 5.0f, 5.12f, 5.20f),
            deviceId = "ESP32-011"
        ),
        Station(
            id = "gin-gampaha",
            name = "Gin Ganga - Baddegama",
            district = "Galle District",
            waterLevel = 2.10,
            rainfall = 22.0,
            ultrasonicDistance = 96.2,
            online = true,
            riskLevel = RiskLevel.LOW,
            latitude = 7.0840,
            longitude = 80.0098,
            chartValues = listOf(1.4f, 1.5f, 1.6f, 1.72f, 1.8f, 1.88f, 1.95f, 2.02f, 2.10f),
            deviceId = "RAIN-003"
        ),
        Station(
            id = "attanagalu-jaela",
            name = "Attanagalu Oya - Ja-Ela",
            district = "Gampaha District",
            waterLevel = 1.15,
            rainfall = 6.2,
            ultrasonicDistance = 124.0,
            online = false,
            riskLevel = RiskLevel.LOW,
            latitude = 7.0744,
            longitude = 79.8919,
            chartValues = listOf(0.9f, 0.95f, 1.0f, 1.08f, 1.15f),
            deviceId = "ESP32-019",
            signal = "Weak"
        )
    )

    val devices = listOf(
        SensorDevice("ESP32-007", "Water + rain node", "Kelani - Seethawaka", DeviceHealth.ONLINE, "Strong", "Just now"),
        SensorDevice("RAIN-003", "Rain gauge", "Gin Ganga - Baddegama", DeviceHealth.ONLINE, "Strong", "2 min ago"),
        SensorDevice("ESP32-011", "Water + rain node", "Kalu - Putupaula", DeviceHealth.CALIBRATION_DUE, "Fair", "8 min ago"),
        SensorDevice("ESP32-019", "Water + rain node", "Attanagalu - Ja-Ela", DeviceHealth.OFFLINE, "None", "14 min ago")
    )

    val managedUsers = listOf(
        ManagedUser("u1", "Nimal Perera", "nimal@email.lk", "Colombo", AccountRole.CITIZEN, SafetyStatus.AT_RISK),
        ManagedUser("u2", "Kavindi Silva", "kavindi@email.lk", "Kalutara", AccountRole.CITIZEN, SafetyStatus.SAFE),
        ManagedUser("u3", "Ruwan Fernando", "ruwan@email.lk", "Gampaha", AccountRole.CITIZEN, SafetyStatus.WATCH),
        ManagedUser("u4", "A. Jayasinghe", "admin@floodsense.lk", "Colombo", AccountRole.ADMIN, SafetyStatus.ACTIVE)
    )

    val adminAlerts = listOf(
        AdminAlert("aa1", "Critical Flood Warning", "Kelani River - Seethawaka", "Water level exceeded the critical limit.", "Just now", 426, AlertSeverity.CRITICAL, AlertDelivery.DELIVERED),
        AdminAlert("aa2", "Water Level Rising", "Kalu Ganga - Millaniya", "Water has risen steadily for 30 minutes.", "15 min ago", 218, AlertSeverity.WARNING, AlertDelivery.ACTIVE),
        AdminAlert("aa3", "Heavy Rainfall Advisory", "Gampaha District", "Rainfall is above the hourly normal range.", "1 hour ago", 640, AlertSeverity.WATCH, AlertDelivery.MONITORING)
    )

    val alerts = listOf(
        FloodAlert(
            id = "a1",
            title = "High Flood Risk",
            stationName = "Kelani River - Seethawaka",
            message = "Flood probability has reached 87%. Move to a safe location and follow official emergency instructions.",
            time = "Just now",
            riskLevel = RiskLevel.HIGH
        ),
        FloodAlert(
            id = "a2",
            title = "Water Level Rising",
            stationName = "Kalu Ganga - Millaniya",
            message = "Water level has increased steadily during the last 30 minutes. Continue monitoring the area.",
            time = "15 min ago",
            riskLevel = RiskLevel.MEDIUM
        ),
        FloodAlert(
            id = "a3",
            title = "Heavy Rainfall Detected",
            stationName = "Gin Ganga - Gampaha",
            message = "Rainfall intensity is above the normal hourly range. Low-lying roads may collect water.",
            time = "1 hour ago",
            riskLevel = RiskLevel.MEDIUM,
            unread = false
        )
    )

    val preparednessItems = listOf(
        PreparednessItem("kit", "Emergency kit ready", "Water, medicine, torch, radio and documents packed", false),
        PreparednessItem("route", "Evacuation route known", "Know two safe routes from your home", false),
        PreparednessItem("contacts", "Emergency contacts saved", "Family and local authority numbers in the app", false),
        PreparednessItem("insurance", "Flood insurance updated", "Review important coverage and documents", false),
        PreparednessItem("structure", "Home structural review", "Check drains, roof and electrical points", false),
        PreparednessItem("station", "Nearest station selected", "Follow the closest Kalu Ganga gauge", false)
    )
}
