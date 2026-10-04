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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floodsense.app.data.FloodAlert
import com.floodsense.app.data.RiskLevel
import com.floodsense.app.data.SensorReading
import com.floodsense.app.data.Station
import com.floodsense.app.data.formatVolts
import com.floodsense.app.data.statusLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.floodsense.app.ui.components.AppHeader
import com.floodsense.app.ui.components.PrimaryGradientButton
import com.floodsense.app.ui.components.RiskBadge
import com.floodsense.app.ui.components.WaveBackground
import com.floodsense.app.ui.theme.BrightBlue
import com.floodsense.app.ui.theme.DeepBlue
import com.floodsense.app.ui.theme.Navy
import com.floodsense.app.ui.theme.PaleBlue
import com.floodsense.app.ui.theme.Teal
import com.floodsense.app.ui.theme.TextSecondary

@Composable
fun StationDetailsScreen(
    station: Station?,
    readings: List<SensorReading>,
    onBack: () -> Unit,
    onPrediction: () -> Unit,
    onAlerts: () -> Unit,
    onPreparedness: () -> Unit
) {
    if (station == null) {
        EmptyDetailScreen("No station selected", onBack)
        return
    }
    val latest = readings.lastOrNull()
    val waterLevel = latest?.waterLevel ?: station.waterLevel
    val rainfall = latest?.rainfall ?: station.rainfall
    val chartValues = readings.map { it.waterLevel.toFloat() }.ifEmpty { station.chartValues }
    val lastUpdated = latest?.let { formatClock(it.timestampMillis) } ?: "waiting for ESP32"
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground(strong = true)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AppHeader(station.name, onBack)
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Water Level", color = TextSecondary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("${"%.2f".format(waterLevel)}", color = Navy, fontWeight = FontWeight.ExtraBold, fontSize = 52.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("metres", color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(station.statusLabel(), color = if (station.online) Teal else Color.Gray)
                        Spacer(Modifier.height(12.dp))
                        RiskBadge(station.riskLevel)
                    }
                }
                Text(
                    if (station.online) "Last sample: $lastUpdated" else station.statusLabel(),
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 20.dp)
                )
                ChartCard(
                    values = chartValues,
                    maxValue = maxOf(10f, chartValues.maxOrNull() ?: 0f),
                    labels = chartTimeLabels(readings)
                )
                Spacer(Modifier.height(22.dp))
                Text("Other Readings", style = MaterialTheme.typography.titleLarge, color = Navy)
                Spacer(Modifier.height(14.dp))
                ReadingCard("Rainfall", "${"%.1f".format(rainfall)} mm", Icons.Outlined.Cloud, DeepBlue)
                Spacer(Modifier.height(12.dp))
                ReadingCard("Ultrasonic Distance", "${"%.1f".format(station.ultrasonicDistance)} cm", Icons.Outlined.ArrowDownward, Teal)
                Spacer(Modifier.height(12.dp))
                ReadingCard("Battery pack", formatVolts(station.batteryVoltage), Icons.Outlined.Bolt, DeepBlue)
                Spacer(Modifier.height(12.dp))
                ReadingCard("Solar panel", formatVolts(station.solarVoltage), Icons.Outlined.WbSunny, Teal)
                Spacer(Modifier.height(22.dp))
                PrimaryGradientButton("Prediction · History & Charts", onPrediction, Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.ShowChart)
                Spacer(Modifier.height(12.dp))
                PrimaryGradientButton("Alerts", onAlerts, Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.NotificationsActive)
                Spacer(Modifier.height(12.dp))
                PrimaryGradientButton("Preparedness Hub", onPreparedness, Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.Shield)
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun ReadingCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(54.dp).background(tint.copy(alpha = 0.10f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(value, color = Navy, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
        }
    }
}

@Composable
fun PredictionScreen(
    station: Station?,
    readings: List<SensorReading>,
    onBack: (() -> Unit)? = null,
    onHistory: () -> Unit
) {
    if (station == null) {
        EmptyDetailScreen("No station selected", onBack ?: {})
        return
    }
    val prediction = station.prediction
    val probability = prediction.probability
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground(strong = true)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            AppHeader("Prediction", onBack, Icons.Outlined.Info) { }
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text("Flood risk prediction", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "${station.riskLevel.label.uppercase()} RISK",
                            color = station.riskLevel.color,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        )
                        Spacer(Modifier.height(22.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Probability", color = Navy, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Text("${(probability * 100).toInt()}%", color = Navy, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = probability,
                            modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
                            color = station.riskLevel.color,
                            trackColor = Color(0xFFDDE5EC)
                        )
                        Spacer(Modifier.height(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Confidence", color = Navy, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Text(prediction.confidence, color = Teal, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("Decision: ${prediction.decision.label}", color = Navy, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Text(prediction.source, color = Teal, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(prediction.action, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                        if (prediction.anomaly) {
                            Spacer(Modifier.height(12.dp))
                            Text("Anomaly detected", color = station.riskLevel.color, fontWeight = FontWeight.Bold)
                            prediction.anomalyReasons.forEach { reason ->
                                Spacer(Modifier.height(4.dp))
                                Text("• $reason", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text("Factors Contributing", style = MaterialTheme.typography.headlineMedium, color = Navy)
                Spacer(Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(Modifier.padding(horizontal = 18.dp)) {
                        FactorRow("Water Level", station.riskLevel, Icons.Outlined.WaterDrop)
                        FactorRow(
                            "Rainfall",
                            when (com.floodsense.app.data.FloodRiskEngine.classifyRisk(0.0, station.rainfall)) {
                                2 -> RiskLevel.HIGH
                                1 -> RiskLevel.MEDIUM
                                else -> RiskLevel.LOW
                            },
                            Icons.Outlined.Cloud
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text("History & Charts", style = MaterialTheme.typography.headlineMedium, color = Navy)
                Spacer(Modifier.height(8.dp))
                Text(station.name, color = Navy, fontWeight = FontWeight.SemiBold)
                var metric by remember { mutableStateOf("Water Level") }
                val liveValues = if (metric == "Water Level") {
                    readings.map { it.waterLevel.toFloat() }
                } else {
                    readings.map { it.rainfall.toFloat() }
                }
                val values = liveValues.ifEmpty {
                    if (metric == "Water Level") station.chartValues else emptyList()
                }
                val max = if (metric == "Water Level") {
                    maxOf(10f, values.maxOrNull() ?: 0f)
                } else {
                    maxOf(200f, values.maxOrNull() ?: 0f)
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Water Level", "Rainfall").forEach { item ->
                        FilterChip(
                            selected = metric == item,
                            onClick = { metric = item },
                            label = { Text(item, modifier = Modifier.padding(horizontal = 12.dp)) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Teal, selectedLabelColor = Color.White)
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                if (values.isEmpty()) {
                    Text("No history samples yet. Charts appear after ESP32 samples or dataset history.", color = TextSecondary)
                } else {
                    ChartCard(
                        values = values,
                        maxValue = max,
                        labels = if (readings.isNotEmpty()) chartTimeLabels(readings) else listOf("History")
                    )
                }
                Spacer(Modifier.height(24.dp))
                PrimaryGradientButton("Open full history", onHistory, Modifier.fillMaxWidth(), leadingIcon = Icons.Outlined.ShowChart)
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun FactorRow(title: String, level: RiskLevel, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).background(PaleBlue, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = DeepBlue)
        }
        Spacer(Modifier.width(14.dp))
        Text(title, color = Navy, fontSize = 18.sp, modifier = Modifier.weight(1f))
        Text(level.label, color = level.color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Icon(Icons.Outlined.ArrowUpward, null, tint = level.color)
    }
}

@Composable
fun HistoryScreen(station: Station?, readings: List<SensorReading>, onBack: () -> Unit) {
    if (station == null) {
        EmptyDetailScreen("No station selected", onBack)
        return
    }
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
        "${formatClock(readings.first().timestampMillis)} - ${formatClock(readings.last().timestampMillis)}"
    } else {
        "Waiting for ESP32 history"
    }

    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            AppHeader("History", onBack)
            HistoryBody(station, readings, metric, values, max, windowLabel) { metric = it }
        }
    }
}

@Composable
private fun HistoryBody(
    station: Station,
    readings: List<SensorReading>,
    metric: String,
    values: List<Float>,
    max: Float,
    windowLabel: String,
    onMetricChange: (String) -> Unit
) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(station.name, color = Navy, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("${readings.size} live samples · $windowLabel", color = TextSecondary)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("Water Level", "Rainfall").forEach { item ->
                        FilterChip(
                            selected = metric == item,
                            onClick = { onMetricChange(item) },
                            label = { Text(item, modifier = Modifier.padding(horizontal = 12.dp)) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Teal, selectedLabelColor = Color.White)
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                if (readings.isEmpty()) {
                    Text("No FloodSystem/history samples yet. Upload the ESP32 sketch to start logging.", color = TextSecondary)
                } else {
                    ChartCard(values = values, maxValue = max, labels = chartTimeLabels(readings))
                }
                Spacer(Modifier.height(24.dp))
                Text("Statistics", style = MaterialTheme.typography.headlineMedium, color = Navy)
                Spacer(Modifier.height(12.dp))
                val average = if (values.isEmpty()) 0f else values.average().toFloat()
                val maximum = values.maxOrNull() ?: 0f
                val minimum = values.minOrNull() ?: 0f
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Average", average, metric, Modifier.weight(1f))
                    StatCard("Maximum", maximum, metric, Modifier.weight(1f))
                    StatCard("Minimum", minimum, metric, Modifier.weight(1f))
                }
                Spacer(Modifier.height(90.dp))
            }
}

@Composable
private fun StatCard(title: String, value: Float, metric: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(if (metric == "Water Level") Icons.Outlined.WaterDrop else Icons.Outlined.Cloud, null, tint = Teal)
            Spacer(Modifier.height(10.dp))
            Text(title, color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                if (metric == "Water Level") "${"%.2f".format(value)} m" else "${"%.1f".format(value)} mm",
                color = Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AlertDetailsScreen(alert: FloodAlert?, onBack: () -> Unit, onOpenStation: () -> Unit) {
    if (alert == null) {
        EmptyDetailScreen("No alert selected", onBack)
        return
    }
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(Modifier.fillMaxSize()) {
            AppHeader("Alert Details", onBack)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = alert.riskLevel.color.copy(alpha = 0.10f))
                ) {
                    Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(78.dp).background(alert.riskLevel.color, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(42.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(alert.title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 26.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Text(alert.stationName, color = TextSecondary, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        RiskBadge(alert.riskLevel)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("What happened", color = Navy, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                        Spacer(Modifier.height(10.dp))
                        Text(alert.message, color = TextSecondary, lineHeight = 23.sp)
                        Spacer(Modifier.height(14.dp))
                        Text("Received ${alert.time}", color = DeepBlue, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("Recommended action", style = MaterialTheme.typography.titleLarge, color = Navy)
                Spacer(Modifier.height(10.dp))
                listOf(
                    "Move people, pets and important documents to a safer level.",
                    "Avoid flooded roads, drains and river banks.",
                    "Follow official instructions from local authorities."
                ).forEachIndexed { index, instruction ->
                    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(28.dp).background(Teal, CircleShape), contentAlignment = Alignment.Center) {
                            Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(instruction, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(24.dp))
                PrimaryGradientButton("Open Station", onOpenStation, Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun RiskMapScreen(stations: List<Station>, onBack: () -> Unit, onStationClick: (Station) -> Unit) {
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(Modifier.fillMaxSize()) {
            AppHeader("Flood Risk Map", onBack)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(18.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFEAF5EC))
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val path = Path().apply {
                        moveTo(size.width * 0.12f, size.height * 0.15f)
                        cubicTo(size.width * 0.30f, size.height * 0.25f, size.width * 0.22f, size.height * 0.48f, size.width * 0.46f, size.height * 0.56f)
                        cubicTo(size.width * 0.63f, size.height * 0.62f, size.width * 0.70f, size.height * 0.40f, size.width * 0.88f, size.height * 0.34f)
                    }
                    drawPath(path, Color(0xFF76BDE2), style = Stroke(width = 24f, cap = StrokeCap.Round))
                    repeat(6) { i ->
                        val y = size.height * (0.10f + i * 0.14f)
                        drawLine(Color.White.copy(alpha = 0.7f), Offset(0f, y), Offset(size.width, y + 30f), 3f)
                    }
                }
                val positions = listOf(
                    0.20f to 0.24f,
                    0.60f to 0.36f,
                    0.34f to 0.57f,
                    0.68f to 0.70f
                )
                stations.take(4).forEachIndexed { index, station ->
                    val (x, y) = positions[index]
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (x * 290).dp, y = (y * 540).dp)
                            .clickable { onStationClick(station) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(46.dp).background(station.riskLevel.color, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.LocationOn, null, tint = Color.White)
                        }
                        Text(station.riskLevel.label, color = station.riskLevel.color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
                Card(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(14.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                ) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        RiskLevel.entries.forEach { level ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(10.dp).background(level.color, CircleShape))
                                Spacer(Modifier.width(5.dp))
                                Text(level.label, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatClock(timestampMillis: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestampMillis))
}

fun chartTimeLabels(readings: List<SensorReading>): List<String> {
    if (readings.isEmpty()) return listOf("Live")
    val points = listOf(0.0, 0.25, 0.5, 0.75, 1.0)
    return points.map { fraction ->
        val index = ((readings.lastIndex) * fraction).toInt().coerceIn(0, readings.lastIndex)
        formatClock(readings[index].timestampMillis)
    }.distinct()
}

@Composable
fun ChartCard(values: List<Float>, maxValue: Float, labels: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row {
                Column(
                    modifier = Modifier.width(48.dp).height(230.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("${maxValue.toInt()}", color = TextSecondary, fontSize = 11.sp)
                    Text("${(maxValue * 0.75f).toInt()}", color = TextSecondary, fontSize = 11.sp)
                    Text("${(maxValue * 0.5f).toInt()}", color = TextSecondary, fontSize = 11.sp)
                    Text("${(maxValue * 0.25f).toInt()}", color = TextSecondary, fontSize = 11.sp)
                    Text("0", color = TextSecondary, fontSize = 11.sp)
                }
                Spacer(Modifier.width(8.dp))
                Canvas(Modifier.weight(1f).height(230.dp)) {
                    repeat(5) { index ->
                        val y = size.height * index / 4f
                        drawLine(Color(0xFFDCEAF3), Offset(0f, y), Offset(size.width, y), 2f)
                    }
                    if (values.size >= 2) {
                        val xStep = size.width / (values.size - 1)
                        val path = Path()
                        values.forEachIndexed { index, value ->
                            val x = index * xStep
                            val y = size.height - (value.coerceIn(0f, maxValue) / maxValue) * size.height
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, BrightBlue, style = Stroke(width = 6f, cap = StrokeCap.Round))
                        val last = values.last().coerceIn(0f, maxValue)
                        val lastY = size.height - (last / maxValue) * size.height
                        drawCircle(Teal, radius = 8f, center = Offset(size.width, lastY))
                        drawCircle(Color.White, radius = 4f, center = Offset(size.width, lastY))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().padding(start = 56.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                labels.forEach { Text(it, color = TextSecondary, fontSize = 10.sp) }
            }
        }
    }
}

@Composable
private fun EmptyDetailScreen(message: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(Modifier.fillMaxSize()) {
            AppHeader("FloodSense", onBack)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(message, color = TextSecondary)
            }
        }
    }
}
