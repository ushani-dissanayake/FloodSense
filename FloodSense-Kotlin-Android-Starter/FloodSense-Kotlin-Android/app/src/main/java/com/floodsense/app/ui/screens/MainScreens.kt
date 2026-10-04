package com.floodsense.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floodsense.app.data.FieldLogCatalog
import com.floodsense.app.data.FieldLogStation
import com.floodsense.app.data.FloodAlert
import com.floodsense.app.data.FloodRiskEngine
import com.floodsense.app.data.RiskLevel
import com.floodsense.app.data.SriLankaDistrict
import com.floodsense.app.data.SriLankaDistricts
import com.floodsense.app.data.Station
import com.floodsense.app.data.statusLabel
import com.floodsense.app.ui.components.CitizenSelectField
import com.floodsense.app.ui.components.FloodSenseLogo
import com.floodsense.app.ui.components.MetricCard
import com.floodsense.app.ui.components.RiskBadge
import com.floodsense.app.ui.components.WaveBackground
import com.floodsense.app.ui.theme.BrightBlue
import com.floodsense.app.ui.theme.DeepBlue
import com.floodsense.app.ui.theme.Navy
import com.floodsense.app.ui.theme.PaleBlue
import com.floodsense.app.ui.theme.Teal
import com.floodsense.app.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    selectedDistrict: String,
    districts: List<SriLankaDistrict> = SriLankaDistricts.home,
    districtStations: List<Station>,
    station: Station?,
    fieldLog: FieldLogStation?,
    alerts: List<FloodAlert>,
    onSelectDistrict: (String) -> Unit,
    onSelectStation: (Station) -> Unit,
    onStationClick: (Station) -> Unit,
    onPredictionClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onMapClick: () -> Unit,
    onPreparednessClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var pickingDistrict by remember { mutableStateOf(false) }
    var pickingStation by remember { mutableStateOf(false) }
    val primaryStation = station
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground(strong = true)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FloodSenseLogo()
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Outlined.AccountCircle, "Profile", tint = DeepBlue, modifier = Modifier.size(34.dp))
                }
            }
            Spacer(Modifier.height(22.dp))
            CitizenSelectField(
                label = "Your district",
                value = selectedDistrict,
                leadingIcon = Icons.Outlined.LocationOn,
                onClick = { pickingDistrict = true }
            )
            Spacer(Modifier.height(16.dp))
            CitizenSelectField(
                label = "Your station",
                value = primaryStation?.name
                    ?: if (districtStations.isEmpty()) "No station in this district"
                    else "Select a station",
                supporting = when (districtStations.size) {
                    0 -> "No Irrigation gauge published here"
                    1 -> "1 station in this district"
                    else -> "${districtStations.size} stations in this district"
                },
                leadingIcon = Icons.Outlined.WaterDrop,
                enabled = districtStations.isNotEmpty(),
                onClick = { pickingStation = true }
            )
            if (fieldLog != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "${fieldLog.river} · ${fieldLog.gauge} gauge",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            Spacer(Modifier.height(20.dp))

            if (primaryStation == null && districtStations.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text("No local station", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Home only lists Kalutara and Ratnapura, the districts in the Kalu Ganga 1000-record dataset.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            primaryStation?.let { station ->
                StationCard(station = station, onClick = { onStationClick(station) }, current = true)
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStationClick(station) },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = station.riskLevel.color),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(horizontal = 22.dp, vertical = 22.dp)) {
                        Text(
                            "Current flood risk",
                            color = Color.White.copy(alpha = 0.86f),
                            fontSize = 13.sp,
                            letterSpacing = 0.4.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "${station.riskLevel.label.uppercase()} RISK",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (station.riskLevel == RiskLevel.HIGH) "Take immediate action" else "Continue monitoring conditions",
                            color = Color.White,
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        )
                        if (station.sourceLabel.isNotBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Text(station.sourceLabel, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (station.online) "Live Overview" else "District overview",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Navy,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        fieldLog?.let { FieldLogCatalog.formatTimestamp(it.latest.timestamp) } ?: "Updated just now",
                        color = BrightBlue,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard(
                        title = "Water Level",
                        value = "${"%.2f".format(station.waterLevel)} m",
                        subtitle = fieldLog?.let {
                            "DMC alert ${"%.1f".format(it.alertM)} m · minor ${"%.1f".format(it.minorM)} m"
                        } ?: "Model: watch ${"%.2f".format(FloodRiskEngine.MEDIUM_WATER_M)} m · high ${"%.2f".format(FloodRiskEngine.HIGH_WATER_M)} m",
                        icon = Icons.Outlined.WaterDrop,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Rainfall",
                        value = "${"%.1f".format(station.rainfall)} mm",
                        subtitle = fieldLog?.let {
                            "Flood peak ${"%.2f".format(it.floodPeak.waterLevel)} m · ${"%.1f".format(it.floodPeak.rainfall)} mm"
                        } ?: "Model: watch 59.97 mm · high 200 mm",
                        icon = Icons.Outlined.Cloud,
                        modifier = Modifier.weight(1f),
                        iconTint = DeepBlue
                    )
                }
                if (fieldLog != null && fieldLog.waterHistory.size >= 2) {
                    Spacer(Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                if (fieldLog.source.contains("Kalu_Ganga")) {
                                    "Kalu Ganga basin 2020–2025"
                                } else {
                                    "Official water-level history"
                                },
                                color = Navy,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "${fieldLog.name} · ${fieldLog.gauge}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(14.dp))
                            DatasetSparkline(
                                values = fieldLog.waterHistory,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                fieldLog.historyLabels.forEach { label ->
                                    Text(label, color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAlertsClick),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(50.dp).background(Color(0xFFFFE9E5), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Notifications, null, tint = Color(0xFFFF3D24))
                    }
                    Spacer(Modifier.width(14.dp))
                    Text("Active Alerts", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    Text("${alerts.count { it.unread }} Active", color = Color(0xFFFF3D24), fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Outlined.ArrowForward, null, tint = DeepBlue)
                }
            }

            Spacer(Modifier.height(22.dp))
            Text("Quick Actions", style = MaterialTheme.typography.titleLarge, color = Navy)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard("Prediction", Icons.Outlined.ShowChart, onPredictionClick, Modifier.weight(1f))
                QuickActionCard("Alerts", Icons.Outlined.Notifications, onAlertsClick, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionCard("Preparedness", Icons.Outlined.Shield, onPreparednessClick, Modifier.weight(1f))
                QuickActionCard("Risk Map", Icons.Outlined.Map, onMapClick, Modifier.weight(1f))
            }
            Spacer(Modifier.height(32.dp))
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
                selectedId = primaryStation?.id,
                districtName = selectedDistrict,
                onSelect = { station ->
                    onSelectStation(station)
                    pickingStation = false
                },
                onDismiss = { pickingStation = false }
            )
        }
    }
}

@Composable
private fun DatasetSparkline(values: List<Float>, modifier: Modifier = Modifier) {
    val maxValue = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val xStep = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * xStep
            val y = size.height - (value.coerceIn(0f, maxValue) / maxValue) * size.height
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, BrightBlue, style = Stroke(width = 5f, cap = StrokeCap.Round))
        val last = values.last().coerceIn(0f, maxValue)
        drawCircle(
            color = Teal,
            radius = 7f,
            center = Offset(size.width, size.height - (last / maxValue) * size.height)
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(PaleBlue, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Teal)
            }
            Spacer(Modifier.width(10.dp))
            Text(title, color = Navy, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StationsScreen(
    currentStation: Station?,
    currentDistrict: String,
    otherStations: List<Station>,
    onStationClick: (Station) -> Unit,
    onMapClick: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val listed = buildList {
        currentStation?.let(::add)
        addAll(otherStations)
    }
    val filtered = when (selectedFilter) {
        "Online" -> listed.filter { it.online }
        "Offline" -> listed.filterNot { it.online }
        else -> listed
    }
    val currentId = currentStation?.id

    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloodSenseLogo(modifier = Modifier.weight(1f), showWordmark = true)
                IconButton(onClick = onMapClick) { Icon(Icons.Outlined.Map, "Map", tint = DeepBlue) }
                IconButton(onClick = { }) { Icon(Icons.Outlined.Search, "Search", tint = Navy) }
            }
            Text("Stations", style = MaterialTheme.typography.headlineLarge, color = Navy, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                currentDistrict,
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("All", "Online", "Offline").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, modifier = Modifier.padding(horizontal = 12.dp)) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val yours = filtered.filter { it.id == currentId }
                val others = filtered.filterNot { it.id == currentId }
                if (yours.isNotEmpty()) {
                    item(key = "header-yours") {
                        Text("Your station", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    items(yours, key = { "y-${it.id}" }) { station ->
                        StationCard(station = station, onClick = { onStationClick(station) }, current = true)
                    }
                }
                if (yours.isEmpty() && others.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            "No station for $currentDistrict in the Kalu Ganga Excel or Irrigation field logs.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
                if (others.isNotEmpty()) {
                    item(key = "header-others") {
                        Text("Other stations in this district", color = Navy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    items(others, key = { it.id }) { station ->
                        StationCard(station = station, onClick = { onStationClick(station) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StationCard(station: Station, onClick: () -> Unit, current: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        border = if (current) androidx.compose.foundation.BorderStroke(2.dp, Teal) else null
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(64.dp)
                    .background(station.riskLevel.color.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.WaterDrop, null, tint = station.riskLevel.color, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                if (current) {
                    Text("YOUR STATION", color = Teal, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(Modifier.height(2.dp))
                }
                Text(station.name, color = Navy, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Text(station.district, color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text("Water Level: ${"%.2f".format(station.waterLevel)} m", color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Text(station.statusLabel(), color = if (station.online) Teal else Color.Gray, fontSize = 12.sp)
            }
            RiskBadge(station.riskLevel)
        }
    }
}

@Composable
fun AlertsScreen(
    alerts: List<FloodAlert>,
    onAlertClick: (FloodAlert) -> Unit
) {
    var filter by remember { mutableStateOf("All") }
    val filtered = when (filter) {
        "Unread" -> alerts.filter { it.unread }
        "Critical" -> alerts.filter { it.riskLevel == RiskLevel.HIGH }
        else -> alerts
    }
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(Modifier.fillMaxSize()) {
            FloodSenseLogo(modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp))
            Text(
                "Alerts",
                style = MaterialTheme.typography.headlineLarge,
                color = Navy,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 6.dp)
            )
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("All", "Unread", "Critical").forEach { item ->
                    FilterChip(
                        selected = filter == item,
                        onClick = { filter = item },
                        label = { Text(item, modifier = Modifier.padding(horizontal = 8.dp)) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp, 20.dp, 20.dp, 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filtered, key = { it.id }) { alert ->
                    AlertCard(alert, onClick = { onAlertClick(alert) })
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: FloodAlert, onClick: () -> Unit) {
    val color = alert.riskLevel.color
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.35f))
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(64.dp).background(color, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("!", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(alert.title, color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(alert.stationName, color = TextSecondary)
                Spacer(Modifier.height(10.dp))
                Text(alert.time, color = DeepBlue, fontSize = 13.sp)
            }
            if (alert.unread) Box(Modifier.size(12.dp).background(color, CircleShape))
        }
    }
}
