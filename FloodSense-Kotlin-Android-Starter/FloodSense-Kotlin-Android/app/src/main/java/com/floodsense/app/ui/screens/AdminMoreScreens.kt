package com.floodsense.app.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floodsense.app.data.AdminAlert
import com.floodsense.app.data.AlertSeverity
import com.floodsense.app.data.FloodRiskEngine
import com.floodsense.app.data.NotificationSettings
import com.floodsense.app.data.Station
import com.floodsense.app.ui.components.AdminHeader
import com.floodsense.app.ui.components.PrimaryGradientButton
import com.floodsense.app.ui.components.RiskBadge
import com.floodsense.app.ui.theme.Danger
import com.floodsense.app.ui.theme.DeepBlue
import com.floodsense.app.ui.theme.Navy
import com.floodsense.app.ui.theme.PaleBlue
import com.floodsense.app.ui.theme.Success
import com.floodsense.app.ui.theme.SurfaceBlue
import com.floodsense.app.ui.theme.Teal
import com.floodsense.app.ui.theme.TextSecondary
import com.floodsense.app.ui.theme.WarningOrange

@Composable
fun AdminAlertDetailsScreen(
    alert: AdminAlert?,
    onBack: () -> Unit,
    onOpenStation: () -> Unit
) {
    if (alert == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No alert selected") }
        return
    }
    val color = when (alert.severity) {
        AlertSeverity.CRITICAL -> Danger
        AlertSeverity.WARNING -> WarningOrange
        else -> Success
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AdminHeader("Alert Details", onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color)
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(64.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Icon(Icons.Outlined.Notifications, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(alert.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(alert.stationName, color = Color.White.copy(alpha = 0.9f))
                    Text(alert.time, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }
            Column(Modifier.padding(20.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("What happened", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(alert.message, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Sent to ${alert.recipients} users · ${alert.delivery.name}", color = DeepBlue, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Recommended action", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                listOf(
                    "Move people and documents to higher ground.",
                    "Avoid flooded roads, drains and river banks.",
                    "Follow official instructions from local authorities."
                ).forEachIndexed { index, instruction ->
                    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(28.dp).background(Teal, CircleShape), contentAlignment = Alignment.Center) {
                            Text("${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(instruction, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(20.dp))
                PrimaryGradientButton("Open Station", onOpenStation, Modifier.fillMaxWidth())
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun AdminPredictionAnalyticsScreen(station: Station?, onBack: () -> Unit) {
    if (station == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Select a station first") }
        return
    }
    val prediction = station.prediction
    val rainShare = (station.rainfall / FloodRiskEngine.highRainfallMm).toFloat().coerceIn(0.08f, 0.72f)
    val waterShare = (1f - rainShare).coerceIn(0.28f, 0.92f)
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AdminHeader("Prediction Analytics", onBack, subtitle = station.name)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(20.dp)) {
                Text(station.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(prediction.source, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Spacer(Modifier.height(14.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(3.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Flood probability (next 24 hours)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${(prediction.probability * 100).toInt()}%",
                            color = prediction.riskLevel.color,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        RiskBadge(prediction.riskLevel)
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = prediction.probability,
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                            color = prediction.riskLevel.color,
                            trackColor = PaleBlue
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Confidence ${prediction.confidence} · ${prediction.decision.label}", color = Teal, fontWeight = FontWeight.SemiBold)
                        Text(prediction.action, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Top factors contributing to the prediction", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                FactorBar("Rainfall (mm)", "${"%.1f".format(station.rainfall)} mm", rainShare, Icons.Outlined.Cloud)
                FactorBar("Water level (m)", "${"%.2f".format(station.waterLevel)} m", waterShare, Icons.Outlined.WaterDrop)
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun FactorBar(title: String, value: String, share: Float, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(icon, null, tint = DeepBlue)
                Spacer(Modifier.width(8.dp))
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(value, color = Teal, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = share,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = Teal,
                trackColor = PaleBlue
            )
        }
    }
}

@Composable
fun AdminEmergencyNotificationsScreen(
    settings: NotificationSettings,
    onBack: () -> Unit,
    onUpdate: (NotificationSettings) -> Unit
) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AdminHeader("Emergency & Notifications", onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(20.dp)) {
                Text("Notifications", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                NotifyRow("Push notifications", "In-app flood warnings", settings.criticalAlerts) {
                    onUpdate(settings.copy(criticalAlerts = it))
                }
                NotifyRow("High risk alerts", "Critical water and rain warnings", settings.risingWaterAlerts) {
                    onUpdate(settings.copy(risingWaterAlerts = it))
                }
                NotifyRow("Rainfall alerts", "When rain crosses the admin threshold", settings.rainfallAlerts) {
                    onUpdate(settings.copy(rainfallAlerts = it))
                }
                NotifyRow("Vibration", "Vibrate for important alerts", settings.vibration) {
                    onUpdate(settings.copy(vibration = it))
                }
                Spacer(Modifier.height(16.dp))
                Text("Emergency contacts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                Text("Tap to call Sri Lanka emergency services.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                EmergencyDialCard("Police / Emergency", "119", Icons.Outlined.Phone, Danger) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:119")))
                }
                EmergencyDialCard("Disaster Management", "117", Icons.Outlined.Shield, DeepBlue) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:117")))
                }
                EmergencyDialCard("Suwa Seriya", "1990", Icons.Outlined.LocalHospital, Success) {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:1990")))
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun NotifyRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Teal)
            )
        }
    }
}

@Composable
private fun EmergencyDialCard(
    title: String,
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(tint.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                androidx.compose.material3.Icon(icon, null, tint = tint)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(number, color = DeepBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
