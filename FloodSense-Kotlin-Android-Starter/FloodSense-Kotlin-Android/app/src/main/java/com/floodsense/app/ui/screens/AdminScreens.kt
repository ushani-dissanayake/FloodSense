package com.floodsense.app.ui.screens

import android.content.Intent
import android.location.Geocoder
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floodsense.app.data.AccountRole
import com.floodsense.app.data.AdminAlert
import com.floodsense.app.data.DeviceLocation
import com.floodsense.app.data.FieldLogCatalog
import com.floodsense.app.data.AlertSeverity
import com.floodsense.app.data.ManagedUser
import com.floodsense.app.data.RiskLevel
import com.floodsense.app.data.SafetyStatus
import com.floodsense.app.data.SensorReading
import com.floodsense.app.data.SriLankaDistrict
import com.floodsense.app.data.SriLankaDistricts
import com.floodsense.app.data.Station
import com.floodsense.app.data.UserProfile
import com.floodsense.app.data.statusLabel
import com.floodsense.app.ui.components.AdminHeader
import com.floodsense.app.ui.components.AdminHeroHeader
import com.floodsense.app.ui.components.FloodField
import com.floodsense.app.ui.components.PrimaryGradientButton
import com.floodsense.app.ui.components.RiskBadge
import com.floodsense.app.ui.components.SettingsRow
import com.floodsense.app.ui.theme.AdminNavy
import com.floodsense.app.ui.theme.Danger
import com.floodsense.app.ui.theme.DeepBlue
import com.floodsense.app.ui.theme.Navy
import com.floodsense.app.ui.theme.PaleBlue
import com.floodsense.app.ui.theme.Success
import com.floodsense.app.ui.theme.SurfaceBlue
import com.floodsense.app.ui.theme.Teal
import com.floodsense.app.ui.theme.TextSecondary
import com.floodsense.app.ui.theme.WarningOrange
import androidx.compose.ui.graphics.Brush
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    selectedDistrict: String,
    districts: List<SriLankaDistrict>,
    districtStations: List<Station>,
    station: Station?,
    onSelectDistrict: (String) -> Unit,
    onSelectStation: (Station) -> Unit,
    onIssueAlert: () -> Unit,
    onStationClick: (Station) -> Unit
) {
    var pickingDistrict by remember { mutableStateOf(false) }
    var pickingStation by remember { mutableStateOf(false) }
    val prediction = station?.prediction
    Box(Modifier.fillMaxSize().background(SurfaceBlue)) {
        Column(Modifier.fillMaxSize()) {
            AdminHeroHeader("Good day, Admin", "Stay informed. Stay prepared.")
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
            AdminSelectField(
                label = "District",
                value = selectedDistrict,
                onClick = { pickingDistrict = true }
            )
            AdminSelectField(
                label = "Selected station",
                value = station?.name ?: "Select a station",
                enabled = districtStations.isNotEmpty(),
                onClick = { pickingStation = true }
            )
            station?.let { focus ->
                AdminRiskBanner(
                    level = focus.riskLevel,
                    probability = prediction?.probability ?: 0f,
                    confidence = prediction?.confidence ?: "—",
                    decision = prediction?.decision?.label ?: "Normal",
                    onClick = { onStationClick(focus) }
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Live sensor summary", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatMini("Water Level", "${"%.2f".format(focus.waterLevel)} m", focus.riskLevel.label, Modifier.weight(1f), tagColor = focus.riskLevel.color)
                        StatMini("Rainfall (24h)", "${"%.1f".format(focus.rainfall)} mm", "Live", Modifier.weight(1f), tagColor = Teal)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Station location", fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp)
                    StationGoogleMap(focus.latitude, focus.longitude, focus.name)
                }
            }
            PrimaryGradientButton("Review & Issue Alert", onIssueAlert, Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.Notifications)
            Spacer(Modifier.height(64.dp))
            }
        }
        if (pickingDistrict) {
            DistrictPickerDialog(
                districts = districts,
                selectedName = selectedDistrict,
                onSelect = { district ->
                    onSelectDistrict(district.name)
                    pickingDistrict = false
                    pickingStation = district.stationIds.isNotEmpty()
                },
                onDismiss = { pickingDistrict = false }
            )
        }
        if (pickingStation) {
            StationPickerDialog(
                stations = districtStations,
                selectedId = station?.id,
                districtName = selectedDistrict,
                onSelect = { picked ->
                    onSelectStation(picked)
                    pickingStation = false
                },
                onDismiss = { pickingStation = false }
            )
        }
    }
}

@Composable
private fun AdminSelectField(
    label: String,
    value: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(1.dp, PaleBlue, RoundedCornerShape(18.dp))
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
            androidx.compose.material3.Icon(Icons.Outlined.KeyboardArrowDown, null, tint = TextSecondary)
        }
    }
}

@Composable
private fun AdminRiskBanner(
    level: RiskLevel,
    probability: Float,
    confidence: String,
    decision: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(AdminNavy, DeepBlue)))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 22.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                "Current flood risk",
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 13.sp,
                letterSpacing = 0.4.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                level.label.uppercase(),
                color = level.color,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${(probability * 100).toInt()}%",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "Confidence $confidence  ·  $decision",
                color = Color.White.copy(alpha = 0.86f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun StatMini(
    title: String,
    value: String,
    tag: String,
    modifier: Modifier = Modifier,
    tagColor: Color = TextSecondary
) {
    Card(
        modifier,
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text(value, color = Navy, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text(tag, color = tagColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AdminSegmentedControl(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, PaleBlue, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val on = option == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (on) AdminNavy else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option,
                    color = if (on) Color.White else Navy,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun AdminStationsScreen(
    stations: List<Station>,
    onStationClick: (Station) -> Unit,
    onAddStation: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    val filtered = stations.filter {
        val matchesQuery = it.name.contains(query, true) || it.district.contains(query, true)
        val matchesFilter = when (filter) {
            "Online" -> it.online
            "Offline" -> !it.online
            "High Risk" -> it.riskLevel == RiskLevel.HIGH
            else -> true
        }
        matchesQuery && matchesFilter
    }
    Box(Modifier.fillMaxSize().background(SurfaceBlue)) {
        Column(Modifier.fillMaxSize()) {
            AdminHeroHeader("Stations", "${stations.size} stations  ·  ${stations.count { it.online }} online")
            Column(Modifier.weight(1f).padding(start = 16.dp, end = 16.dp, top = 20.dp)) {
        FloodField(
            label = "",
            value = query,
            onValueChange = { query = it },
            placeholder = "Search station or district",
            leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.Search, null) }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("All", "Online", "Offline", "High Risk").forEach { chip ->
                item { FilterChip(selected = filter == chip, onClick = { filter = chip }, label = { Text(chip) }) }
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            filtered.forEach { station ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onStationClick(station) },
                    colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(station.name, fontWeight = FontWeight.Bold, color = Navy)
                            Text(station.district, color = TextSecondary, fontSize = 12.sp)
                            Text(if (station.online) "${station.waterLevel} m" else "--", color = DeepBlue, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(station.statusLabel(), color = if (station.online) Success else Danger, fontWeight = FontWeight.Bold)
                            RiskBadge(station.riskLevel)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            PrimaryGradientButton("+ Add Station", onAddStation, Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.Add)
            Spacer(Modifier.height(80.dp))
            }
        }
        }
    }
}

@Composable
fun AdminAddStationScreen(
    stations: List<Station>,
    onBack: () -> Unit,
    onSave: (Station) -> Unit
) {
    val homeDistricts = remember { SriLankaDistricts.home }
    val allDistricts = remember { SriLankaDistricts.all }
    val stationOptions = remember(homeDistricts) {
        homeDistricts
            .flatMap { district -> district.stationIds }
            .distinct()
            .mapNotNull { id -> FieldLogCatalog.station(id) }
    }

    var selectedStationId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var district by remember {
        mutableStateOf(allDistricts.firstOrNull()?.name ?: SriLankaDistricts.DEFAULT)
    }
    var lat by remember { mutableStateOf("") }
    var lon by remember { mutableStateOf("") }
    var warning by remember { mutableStateOf("") }
    var critical by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val latitude = lat.toDoubleOrNull()
    val longitude = lon.toDoubleOrNull()
    val showMap = latitude != null && longitude != null

    var gpsMessage by remember { mutableStateOf<String?>(null) }

    fun connectSelectedStationGps() {
        if (selectedStationId.isBlank()) {
            error = "Please select a station first."
            gpsMessage = null
            return
        }

        val selectedStation = FieldLogCatalog.station(selectedStationId)
        if (selectedStation == null) {
            error = "Location is not available for the selected station."
            gpsMessage = null
            return
        }

        lat = "%.6f".format(java.util.Locale.US, selectedStation.latitude)
        lon = "%.6f".format(java.util.Locale.US, selectedStation.longitude)
        error = null
        gpsMessage = "Station location connected: ${selectedStation.name}"
    }

    Column(Modifier.fillMaxSize().background(SurfaceBlue)) {
        AdminHeader("Add New Station", onBack = onBack, subtitle = "Register a monitoring location")
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            StationDropdownField(
                selectedStationId = selectedStationId,
                stations = stationOptions,
                onSelect = { selected ->
                    selectedStationId = selected.id
                    name = selected.name
                    lat = ""
                    lon = ""
                    gpsMessage = null
                    // District is intentionally NOT changed here.
                    // The admin selects the district manually.
                    error = null
                }
            )

            DistrictDropdownField(
                selected = district,
                districts = allDistricts,
                onSelect = { selectedDistrict ->
                    district = selectedDistrict
                    // Keep the selected station unchanged; station and district
                    // are independent choices on this screen.
                    error = null
                }
            )

            PrimaryGradientButton(
                "Connect GPS",
                { connectSelectedStationGps() },
                Modifier.fillMaxWidth(),
                enabled = selectedStationId.isNotBlank(),
                leadingIcon = Icons.Outlined.MyLocation
            )

            gpsMessage?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(
                    message,
                    color = Success,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))
            FormField("Latitude", lat, KeyboardOptions(keyboardType = KeyboardType.Decimal)) {
                lat = it
                error = null
            }
            FormField("Longitude", lon, KeyboardOptions(keyboardType = KeyboardType.Decimal)) {
                lon = it
                error = null
            }

            // Show the map on this same page as soon as GPS (or manual coordinates) gives valid values.
            if (showMap) {
                Spacer(Modifier.height(12.dp))
                Text("Station location preview", color = Navy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(8.dp))
                StationGoogleMap(
                    latitude = latitude!!,
                    longitude = longitude!!,
                    stationName = name.ifBlank { "New station" },
                    modifier = Modifier.fillMaxWidth(),
                    snapToStation = false
                )
                Spacer(Modifier.height(8.dp))
            }

            // Warning and critical levels are intentionally manual inputs.
            FormField("Warning level (m)", warning, KeyboardOptions(keyboardType = KeyboardType.Decimal)) {
                warning = it
                error = null
            }
            FormField("Critical level (m)", critical, KeyboardOptions(keyboardType = KeyboardType.Decimal)) {
                critical = it
                error = null
            }

            error?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(message, color = Danger, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(16.dp))
            PrimaryGradientButton("Save Station", {
                val stationId = selectedStationId.trim()
                val stationName = name.trim()
                val warningLevel = warning.toDoubleOrNull()
                val criticalLevel = critical.toDoubleOrNull()
                val duplicate = stations.any { current ->
                    current.id.equals(stationId, ignoreCase = true) ||
                        current.name.equals(stationName, ignoreCase = true)
                }

                error = when {
                    stationId.isBlank() || stationName.isBlank() -> "Select a station name."
                    latitude == null || longitude == null -> "Connect GPS or enter valid latitude and longitude."
                    warningLevel == null -> "Enter a valid warning level."
                    criticalLevel == null -> "Enter a valid critical level."
                    criticalLevel <= warningLevel -> "Critical level must be higher than warning level."
                    duplicate -> "This station already exists."
                    else -> null
                }

                if (error != null || latitude == null || longitude == null || warningLevel == null || criticalLevel == null) {
                    return@PrimaryGradientButton
                }

                onSave(
                    Station(
                        id = stationId,
                        name = stationName,
                        district = district,
                        waterLevel = 0.0,
                        rainfall = 0.0,
                        ultrasonicDistance = 0.0,
                        online = true,
                        riskLevel = RiskLevel.LOW,
                        latitude = latitude,
                        longitude = longitude,
                        chartValues = listOf(0f),
                        warningLevel = warningLevel,
                        criticalLevel = criticalLevel
                    )
                )
            }, Modifier.fillMaxWidth())

            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Cancel")
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StationDropdownField(
    selectedStationId: String,
    stations: List<com.floodsense.app.data.FieldLogStation>,
    onSelect: (com.floodsense.app.data.FieldLogStation) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = stations.firstOrNull { it.id == selectedStationId }

    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            "Station Name",
            color = Navy,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
        )
        Box {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selected?.name ?: "Select station",
                        color = if (selected == null) TextSecondary else DeepBlue,
                        modifier = Modifier.weight(1f),
                        fontSize = 16.sp,
                        fontWeight = if (selected == null) FontWeight.Normal else FontWeight.SemiBold
                    )
                    Icon(
                        Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Select station",
                        tint = Teal
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 360.dp)
            ) {
                if (stations.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No Home-page stations available", color = TextSecondary) },
                        onClick = { expanded = false }
                    )
                } else {
                    stations.forEach { station ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(station.name, color = DeepBlue, fontWeight = FontWeight.SemiBold)
                                    Text(station.district, color = TextSecondary, fontSize = 12.sp)
                                }
                            },
                            onClick = {
                                onSelect(station)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onChange: (String) -> Unit
) {
    FloodField(
        label = label,
        value = value,
        onValueChange = onChange,
        placeholder = label,
        keyboardOptions = keyboardOptions,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
fun AdminStationDetailsScreen(
    station: Station?,
    onBack: () -> Unit,
    onViewHistory: () -> Unit,
    onSendAlert: () -> Unit
) {
    if (station == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No station selected") }
        return
    }
    Column(Modifier.fillMaxSize().background(SurfaceBlue)) {
        AdminHeader(station.name, onBack = onBack, subtitle = station.district)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RiskBadge(station.riskLevel)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Teal)
                        .clickable(onClick = onViewHistory)
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text("View", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("Current water level", color = TextSecondary)
            Text("${station.waterLevel} m", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text("Warning ${station.warningLevel} m  ·  Critical ${station.criticalLevel} m", color = TextSecondary)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatMini("Rainfall", "${station.rainfall} mm", "Live", Modifier.weight(1f))
                StatMini("Ultrasonic", "${"%.1f".format(station.ultrasonicDistance)} cm", "Distance", Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            Text("Station location", fontWeight = FontWeight.Bold, color = Navy)
            Spacer(Modifier.height(8.dp))
            StationGoogleMap(station.latitude, station.longitude, station.name)
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(Danger).clickable(onClick = onSendAlert),
                contentAlignment = Alignment.Center
            ) { Text("Send Alert", color = Color.White, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun AdminAlertsScreen(alerts: List<AdminAlert>, onCreate: () -> Unit, onAlertClick: (AdminAlert) -> Unit) {
    var filter by remember { mutableStateOf("All") }
    val filtered = alerts.filter {
        when (filter) {
            "Critical" -> it.severity == AlertSeverity.CRITICAL
            "Active" -> it.delivery.name != "RESOLVED"
            "Resolved" -> it.delivery.name == "RESOLVED"
            else -> true
        }
    }
    Column(Modifier.fillMaxSize().background(SurfaceBlue)) {
        AdminHeroHeader("Alerts", "Stay informed. Stay prepared.")
        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatMini("Active", "${alerts.size}", "Alerts", Modifier.weight(1f))
            StatMini("Critical", "${alerts.count { it.severity == AlertSeverity.CRITICAL }}", "Now", Modifier.weight(1f))
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("All", "Active", "Resolved", "Critical").forEach { chip ->
                item { FilterChip(selected = filter == chip, onClick = { filter = chip }, label = { Text(chip) }) }
            }
        }
        filtered.forEach { alert ->
            Card(
                Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onAlertClick(alert) },
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text(alert.title, fontWeight = FontWeight.Bold, color = Navy)
                    Text(alert.stationName, color = TextSecondary)
                    Text("Sent to ${alert.recipients} users · ${alert.time}", color = TextSecondary, fontSize = 12.sp)
                    Text(alert.delivery.name, color = Danger, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        PrimaryGradientButton("+ Create Alert", onCreate, Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.Add)
        Spacer(Modifier.height(80.dp))
    }
    }
}

@Composable
fun AdminCreateAlertScreen(
    stations: List<Station>,
    selectedStationId: String? = null,
    onBack: () -> Unit,
    onSend: (String, String, String, AlertSeverity, Boolean) -> Unit
) {
    var severity by remember { mutableStateOf(AlertSeverity.CRITICAL) }
    var selectedId by remember {
        mutableStateOf(selectedStationId ?: stations.firstOrNull()?.id)
    }
    var title by remember { mutableStateOf("High Flood Risk - Immediate Action") }
    var message by remember { mutableStateOf("Water level has exceeded the critical limit. Move to higher ground.") }
    val selectedStation = stations.firstOrNull { it.id == selectedId } ?: stations.firstOrNull()
    Column(Modifier.fillMaxSize().background(SurfaceBlue)) {
        AdminHeader("Create Emergency Alert", onBack = onBack, subtitle = "Notify users in affected areas")
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AlertSeverity.entries.forEach { level ->
                val selected = severity == level
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) PaleBlue else Color.White)
                        .border(1.dp, if (selected) Teal else Color(0xFFD5E1EC), RoundedCornerShape(12.dp))
                        .clickable { severity = level }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) { Text(level.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Navy) }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Affected station", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        if (stations.isNotEmpty()) {
            stations.forEach { option ->
                val selected = option.id == selectedStation?.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { selectedId = option.id },
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        if (selected) 2.dp else 1.dp,
                        if (selected) Teal else PaleBlue
                    )
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Text(option.name, color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(option.district, color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            FormField("Station name", selectedStation?.name.orEmpty()) { }
        }
        FormField("Alert title", title) { title = it }
        FormField("Message", message) { message = it }
        Text(
            if (severity == AlertSeverity.CRITICAL) {
                "Critical alerts send FCM and up to 2 emergency SMS (text.lk free quota)."
            } else {
                "SMS is sent only for Critical emergency alerts. This level stays in the app."
            },
            color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().background(Danger.copy(0.12f), RoundedCornerShape(12.dp)).padding(10.dp)) {
            Text("This alert will be sent immediately.", color = Danger, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(Danger).clickable {
                onSend(title, selectedStation?.name.orEmpty(), message, severity, false)
            },
            contentAlignment = Alignment.Center
        ) { Text("SEND EMERGENCY ALERT", color = Color.White, fontWeight = FontWeight.Bold) }
    }
    }
}

@Composable
fun AdminUsersScreen(users: List<ManagedUser>, onAdd: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    val filtered = users.filter {
        val q = it.name.contains(query, true) || it.email.contains(query, true) ||
            it.district.contains(query, true) || it.townCity.contains(query, true)
        val f = when (filter) {
            "Citizens" -> it.role == AccountRole.CITIZEN
            "Admins" -> it.role == AccountRole.ADMIN
            "At Risk" -> it.status == SafetyStatus.AT_RISK
            else -> true
        }
        q && f
    }
    Column(Modifier.fillMaxSize().background(SurfaceBlue)) {
        AdminHeroHeader("Users", "${users.size} accounts")
        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatMini("Total", "${users.size}", "Users", Modifier.weight(1f))
            StatMini("At risk", "${users.count { it.status == SafetyStatus.AT_RISK }}", "Now", Modifier.weight(1f))
            StatMini("Admins", "${users.count { it.role == AccountRole.ADMIN }}", "Staff", Modifier.weight(1f))
        }
        FloodField(
            label = "",
            value = query,
            onValueChange = { query = it },
            placeholder = "Search name, email, or district",
            leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.Search, null) },
            modifier = Modifier.padding(vertical = 8.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Citizens", "Admins", "At Risk").forEach { chip ->
                item { FilterChip(selected = filter == chip, onClick = { filter = chip }, label = { Text(chip) }) }
            }
        }
        filtered.forEach { user ->
            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).background(PaleBlue, CircleShape), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Icon(Icons.Outlined.Person, null, tint = DeepBlue)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user.name, fontWeight = FontWeight.Bold, color = Navy)
                        Text(user.email, color = DeepBlue, fontSize = 12.sp)
                        Text(
                            listOfNotNull(
                                user.townCity.takeIf { town -> town.isNotBlank() },
                                user.district
                            ).joinToString(" · "),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        if (user.latitude != null && user.longitude != null) {
                            Text(
                                "Latitude  ${"%.6f".format(user.latitude)}",
                                color = Navy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Longitude  ${"%.6f".format(user.longitude)}",
                                color = Navy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(user.role.name, color = DeepBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(user.status.label, color = when (user.status) {
                            SafetyStatus.AT_RISK -> Danger
                            SafetyStatus.WATCH -> WarningOrange
                            else -> Success
                        }, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        PrimaryGradientButton("+ Add User", onAdd, Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.Add)
        Spacer(Modifier.height(80.dp))
    }
    }
}

@Composable
fun AdminAddUserScreen(onBack: () -> Unit, onSave: (ManagedUser) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var district by remember { mutableStateOf(SriLankaDistricts.all.first().name) }
    var townCity by remember { mutableStateOf("") }
    var latitudeText by remember { mutableStateOf("") }
    var longitudeText by remember { mutableStateOf("") }
    val latitude = latitudeText.toDoubleOrNull()
    val longitude = longitudeText.toDoubleOrNull()
    val context = LocalContext.current
    var locationMessage by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    fun connectTownLocation() {
        if (townCity.isBlank()) {
            locationError = "Please enter Town / City first."
            locationMessage = null
            return
        }
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val searchAddress = "$townCity, $district, Sri Lanka"
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(searchAddress, 1)
            val location = addresses?.firstOrNull()
            if (location != null) {
                latitudeText = "%.6f".format(Locale.US, location.latitude)
                longitudeText = "%.6f".format(Locale.US, location.longitude)
                locationMessage = "Location connected: $townCity, $district"
                locationError = null
            } else {
                latitudeText = ""
                longitudeText = ""
                locationMessage = null
                locationError = "Could not find this Town / City. Check the name and try again."
            }
        } catch (e: Exception) {
            latitudeText = ""
            longitudeText = ""
            locationMessage = null
            locationError = "Unable to find location. Check your internet connection."
        }
    }
    Column(Modifier.fillMaxSize().background(SurfaceBlue)) {
        AdminHeader("Add User", onBack = onBack)
        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            FormField("Full name", name) { name = it }
            FormField("Email", email) { email = it }
            DistrictDropdownField(
                selected = district,
                districts = SriLankaDistricts.all,
                onSelect = {
                    district = it
                    latitudeText = ""
                    longitudeText = ""
                    locationMessage = null
                    locationError = null
                }
            )
            FormField("Town / City", townCity) {
                townCity = it
                latitudeText = ""
                longitudeText = ""
                locationMessage = null
                locationError = null
            }
            PrimaryGradientButton(
                "Connect GPS",
                { connectTownLocation() },
                Modifier.fillMaxWidth(),
                enabled = townCity.isNotBlank(),
                leadingIcon = Icons.Outlined.MyLocation
            )
            locationMessage?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(message, color = Success, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            locationError?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(message, color = Danger, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            FormField(
                "Latitude",
                latitudeText,
                KeyboardOptions(keyboardType = KeyboardType.Decimal)
            ) { latitudeText = it }
            FormField(
                "Longitude",
                longitudeText,
                KeyboardOptions(keyboardType = KeyboardType.Decimal)
            ) { longitudeText = it }
            if (latitude != null && longitude != null) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatMini("Latitude", "%.6f".format(latitude), "North/South", Modifier.weight(1f), Teal)
                    StatMini("Longitude", "%.6f".format(longitude), "East/West", Modifier.weight(1f), Teal)
                }
                Spacer(Modifier.height(12.dp))
                Text("Linked location", fontWeight = FontWeight.Bold, color = Navy)
                Spacer(Modifier.height(8.dp))
                StationGoogleMap(
                    latitude = latitude,
                    longitude = longitude,
                    stationName = townCity.ifBlank { district },
                    snapToStation = false
                )
            }
            Spacer(Modifier.height(16.dp))
            PrimaryGradientButton("Save User", {
                if (name.isNotBlank() && email.contains("@")) {
                    onSave(
                        ManagedUser(
                            id = "u-${System.currentTimeMillis()}",
                            name = name.trim(),
                            email = email.trim(),
                            district = district,
                            role = AccountRole.CITIZEN,
                            status = SafetyStatus.SAFE,
                            townCity = townCity.trim(),
                            latitude = latitude,
                            longitude = longitude
                        )
                    )
                }
            }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(80.dp))
        }
    }
}

private data class GpsFillState(
    val busy: Boolean,
    val message: String?,
    val connect: () -> Unit
)

@Composable
private fun rememberGpsFill(onFill: (String, String) -> Unit): GpsFillState {
    val context = LocalContext.current
    val fill by rememberUpdatedState(onFill)
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val requestPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) {
            requestGpsFix(context, { busy = it }, { message = it }, fill)
        } else {
            message = "Allow location permission, then tap Connect GPS again."
        }
    }
    return GpsFillState(
        busy = busy,
        message = message,
        connect = {
            when {
                !DeviceLocation.hasPermission(context) ->
                    requestPermission.launch(DeviceLocation.permissions)
                !DeviceLocation.isEnabled(context) -> {
                    message = "Turn on Location, then tap Connect GPS again."
                    runCatching {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                }
                else -> requestGpsFix(context, { busy = it }, { message = it }, fill)
            }
        }
    )
}

private fun requestGpsFix(
    context: android.content.Context,
    setBusy: (Boolean) -> Unit,
    setMessage: (String) -> Unit,
    onFill: (String, String) -> Unit
) {
    setBusy(true)
    setMessage("Connecting GPS…")
    DeviceLocation.requestCurrent(context) { location ->
        setBusy(false)
        if (location == null) {
            setMessage("No GPS fix yet. On a phone turn Location on. On an emulator set a point in Extended controls → Location.")
        } else {
            onFill("%.6f".format(location.latitude), "%.6f".format(location.longitude))
            setMessage("GPS connected. Latitude and longitude were filled.")
        }
    }
}

@Composable
private fun DistrictDropdownField(
    selected: String,
    districts: List<SriLankaDistrict>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            "District",
            color = Navy,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
        )
        Box {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selected, color = DeepBlue, modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = "Select district", tint = Teal)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 320.dp)
            ) {
                districts.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(item.name, color = DeepBlue, fontWeight = FontWeight.SemiBold)
                                Text(item.province, color = TextSecondary, fontSize = 12.sp)
                            }
                        },
                        onClick = {
                            onSelect(item.name)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminReportsScreen(
    readings: List<SensorReading>,
    stations: List<Station>,
    adminAlerts: List<AdminAlert>,
    onOpenHistory: (Station) -> Unit = {}
) {
    val liveNodeIds = readings.map { it.stationId }.filter { it.isNotBlank() }.distinct()
    val high = readings.count { it.floodRisk >= 2 }
    val medium = readings.count { it.floodRisk == 1 }
    val low = readings.count { it.floodRisk == 0 }
    val total = readings.size
    val window = if (readings.size >= 2) {
        "${chartTimeLabels(readings).first()} to ${chartTimeLabels(readings).last()}"
    } else {
        "Waiting for ESP32 history"
    }
    val ranked = stations.sortedWith(
        compareByDescending<Station> { it.riskLevel.ordinal }.thenByDescending { it.waterLevel }
    )
    val liveStations = stations.filter { station -> readings.any { it.stationId == station.id } }
    Column(Modifier.fillMaxSize().background(SurfaceBlue)) {
        AdminHeroHeader(
            "Reports & Analytics",
            "${liveNodeIds.size} live ESP32 nodes  ·  $window"
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatMini("Samples", "${readings.size}", if (readings.isEmpty()) "No history" else "All history", Modifier.weight(1f), tagColor = DeepBlue)
                StatMini("Alerts issued", "${adminAlerts.size}", "This session", Modifier.weight(1f), tagColor = Teal)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatMini("Live nodes", "${liveNodeIds.size}", "With history", Modifier.weight(1f), tagColor = Teal)
                StatMini("High samples", "$high", if (total == 0) "No data" else "${high * 100 / total.coerceAtLeast(1)}% of history", Modifier.weight(1f), tagColor = Danger)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Risk distribution", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (readings.isEmpty()) {
                        Text("No history samples yet. Charts and percentages appear after ESP32 uploads.", color = TextSecondary, lineHeight = 20.sp)
                    } else {
                        AdminRiskBar("Low", low, total, Success)
                        AdminRiskBar("Medium", medium, total, WarningOrange)
                        AdminRiskBar("High", high, total, Danger)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Live nodes", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (liveStations.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("No per-station history yet", color = Navy, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            Text("Upload an ESP32 sketch with STATION_ID set to start node reports.", color = TextSecondary, lineHeight = 20.sp)
                        }
                    }
                } else {
                    liveStations.forEach { station ->
                        val stationReadings = readings.filter { it.stationId == station.id }
                        val latest = stationReadings.lastOrNull() ?: return@forEach
                        AdminReportStationRow(
                            station = station,
                            samples = stationReadings.size,
                            waterLevel = latest.waterLevel,
                            rainfall = latest.rainfall,
                            onClick = { onOpenHistory(station) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Current station risk", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (ranked.isEmpty()) {
                    Text("No stations available.", color = TextSecondary)
                } else {
                    ranked.take(5).forEachIndexed { index, station ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenHistory(station) },
                            colors = CardDefaults.cardColors(Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(36.dp).background(PaleBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${index + 1}", color = AdminNavy, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(station.name, color = Navy, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${station.district}  ·  ${"%.2f".format(station.waterLevel)} m  ·  ${"%.1f".format(station.rainfall)} mm",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                                RiskBadge(station.riskLevel)
                            }
                        }
                    }
                }
            }

            if (adminAlerts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Recent alerts", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    adminAlerts.take(5).forEach { alert ->
                        val color = when (alert.severity) {
                            AlertSeverity.CRITICAL -> Danger
                            AlertSeverity.WARNING -> WarningOrange
                            else -> Success
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(12.dp).background(color, CircleShape)
                                )
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(alert.title, color = Navy, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${alert.stationName}  ·  ${alert.time}",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AdminRiskBar(label: String, count: Int, total: Int, color: Color) {
    val percent = if (total <= 0) 0 else count * 100 / total
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Navy, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text("$count  ·  $percent%", color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        LinearProgressIndicator(
            progress = (percent / 100f).coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
            color = color,
            trackColor = PaleBlue
        )
    }
}

@Composable
private fun AdminReportStationRow(
    station: Station,
    samples: Int,
    waterLevel: Double,
    rainfall: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(46.dp).background(PaleBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.WaterDrop, null, tint = DeepBlue)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(station.name, color = Navy, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("$samples samples", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "${"%.2f".format(waterLevel)} m  ·  ${"%.1f".format(rainfall)} mm",
                    color = DeepBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            RiskBadge(station.riskLevel)
        }
    }
}

@Composable
fun AdminHistoryScreen(
    station: Station?,
    stations: List<Station>,
    readings: List<SensorReading>,
    onBack: () -> Unit,
    onSelectStation: (Station) -> Unit
) {
    var metric by remember { mutableStateOf("Water Level") }
    val values = if (metric == "Water Level") {
        readings.map { it.waterLevel.toFloat() }
    } else {
        readings.map { it.rainfall.toFloat() }
    }
    val max = if (metric == "Water Level") {
        maxOf(10f, values.maxOrNull() ?: 0f)
    } else {
        maxOf(200f, values.maxOrNull() ?: 0f)
    }
    val windowLabel = if (readings.size >= 2) {
        "${chartTimeLabels(readings).first()} – ${chartTimeLabels(readings).last()}"
    } else {
        "Waiting for ESP32 history"
    }
    val average = if (values.isEmpty()) 0f else values.average().toFloat()
    val maximum = values.maxOrNull() ?: 0f
    val minimum = values.minOrNull() ?: 0f
    val delta = if (values.size >= 2) values.last() - values.first() else 0f
    val trend = when {
        values.size < 2 -> "Not enough samples"
        delta > 0.05f -> "Rising over this window"
        delta < -0.05f -> "Falling over this window"
        else -> "Stable over this window"
    }
    var pickingStation by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(SurfaceBlue)) {
        Column(Modifier.fillMaxSize()) {
        AdminHeader("History & Trends", onBack, subtitle = station?.name ?: "Select a station")
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (stations.isNotEmpty()) {
                AdminSelectField(
                    label = "Station",
                    value = station?.name ?: "Select a station",
                    onClick = { pickingStation = true }
                )
            }
            if (station == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text("No station selected", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Choose a gauge to inspect water-level and rainfall history.", color = TextSecondary, lineHeight = 20.sp)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(52.dp).background(PaleBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.ShowChart, null, tint = AdminNavy)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(station.name, color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(station.district, color = TextSecondary, fontSize = 13.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(station.statusLabel(), color = if (station.online) Success else TextSecondary, fontSize = 13.sp)
                        }
                        RiskBadge(station.riskLevel)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatMini(
                        "Water level",
                        "${"%.2f".format(station.waterLevel)} m",
                        station.riskLevel.label,
                        Modifier.weight(1f),
                        tagColor = station.riskLevel.color
                    )
                    StatMini(
                        "Rainfall",
                        "${"%.1f".format(station.rainfall)} mm",
                        "${readings.size} samples",
                        Modifier.weight(1f),
                        tagColor = DeepBlue
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Trend chart", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    AdminSegmentedControl(
                        options = listOf("Water Level", "Rainfall"),
                        selected = metric,
                        onSelect = { metric = it }
                    )
                    if (readings.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(Modifier.padding(22.dp)) {
                                Text("No samples yet", color = Navy, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "FloodSystem/history appears after the ESP32 sketch starts logging.",
                                    color = TextSecondary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    } else {
                        ChartCard(values = values, maxValue = max, labels = chartTimeLabels(readings))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Statistics", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatMini("Average", formatHistoryStat(average, metric), "Mean", Modifier.weight(1f), tagColor = Teal)
                        StatMini("Maximum", formatHistoryStat(maximum, metric), "Peak", Modifier.weight(1f), tagColor = Danger)
                        StatMini("Minimum", formatHistoryStat(minimum, metric), "Low", Modifier.weight(1f), tagColor = DeepBlue)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Window insight", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(trend, color = AdminNavy, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        Text("$windowLabel  ·  ${readings.size} live samples", color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
        }
        if (pickingStation) {
            StationPickerDialog(
                stations = stations,
                selectedId = station?.id,
                districtName = "all districts",
                onSelect = {
                    onSelectStation(it)
                    pickingStation = false
                },
                onDismiss = { pickingStation = false }
            )
        }
    }
}

private fun formatHistoryStat(value: Float, metric: String): String {
    return if (metric == "Water Level") "%.2f m".format(value) else "%.1f mm".format(value)
}

@Composable
fun AdminMoreScreen(
    onHistory: () -> Unit,
    onPrediction: () -> Unit,
    onEmergency: () -> Unit,
    onReports: () -> Unit,
    onSettings: () -> Unit,
    onProfile: () -> Unit,
    onOnboarding: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(SurfaceBlue)) {
        AdminHeroHeader("More")
        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
            SettingsRow("History", Icons.Outlined.History, onHistory, subtitle = "Water level and rainfall charts")
            SettingsRow("Prediction analytics", Icons.Outlined.ShowChart, onPrediction, subtitle = "Water and rainfall factors")
            SettingsRow("Emergency & notifications", Icons.Outlined.Notifications, onEmergency)
            SettingsRow("Admin profile", Icons.Outlined.Person, onProfile, subtitle = "Update administrator details")
            SettingsRow("System settings", Icons.Outlined.Settings, onSettings)
            SettingsRow("Reports", Icons.Outlined.WaterDrop, onReports)
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun AdminSettingsScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    onEmergency: () -> Unit,
    onPrediction: () -> Unit,
    onLogout: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(SurfaceBlue)) {
        AdminHeader("System Settings", onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Card(
                Modifier.padding(16.dp).fillMaxWidth().clickable(onClick = onProfile),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(profile.fullName, fontWeight = FontWeight.Bold, color = Navy, fontSize = 20.sp)
                    Text(profile.email, color = DeepBlue)
                    Text("Tap to update profile", color = Teal, fontWeight = FontWeight.Bold)
                }
            }
            Text("System configuration", Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold, color = Navy)
            SettingsRow("Notification Channels", Icons.Outlined.Notifications, onEmergency)
            SettingsRow("Prediction Analytics", Icons.Outlined.ShowChart, onPrediction)
            Text("Account", Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontWeight = FontWeight.Bold, color = Navy)
            SettingsRow("Admin profile", Icons.Outlined.Person, onProfile, subtitle = "Name, email, district")
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.padding(16.dp).fillMaxWidth().height(52.dp).border(1.dp, Danger, RoundedCornerShape(16.dp)).clickable(onClick = onLogout),
                contentAlignment = Alignment.Center
            ) { Text("Sign Out", color = Danger, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(80.dp))
        }
    }
}
