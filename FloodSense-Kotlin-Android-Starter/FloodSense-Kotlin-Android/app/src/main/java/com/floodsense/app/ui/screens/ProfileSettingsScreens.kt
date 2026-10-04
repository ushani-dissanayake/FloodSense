package com.floodsense.app.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Directions
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floodsense.app.data.EmergencyContact
import com.floodsense.app.data.DeviceLocation
import com.floodsense.app.data.NotificationSettings
import com.floodsense.app.data.PreparednessItem
import com.floodsense.app.data.SriLankaDistricts
import com.floodsense.app.data.Station
import com.floodsense.app.data.ThemeMode
import com.floodsense.app.data.UserProfile
import com.floodsense.app.ui.components.AdminHeader
import com.floodsense.app.ui.components.AppHeader
import com.floodsense.app.ui.components.ChecklistRow
import com.floodsense.app.ui.components.FloodField
import com.floodsense.app.ui.components.FloodFormSheet
import com.floodsense.app.ui.components.FloodSenseLogo
import com.floodsense.app.ui.components.PrimaryGradientButton
import com.floodsense.app.ui.components.SettingsRow
import com.floodsense.app.ui.components.WaveBackground
import com.floodsense.app.ui.theme.BrightBlue
import com.floodsense.app.ui.theme.Cyan
import com.floodsense.app.ui.theme.DeepBlue
import com.floodsense.app.ui.theme.Navy
import com.floodsense.app.ui.theme.PaleBlue
import com.floodsense.app.ui.theme.SurfaceBlue
import com.floodsense.app.ui.theme.Teal
import com.floodsense.app.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onSettings: () -> Unit,
    onPersonalInformation: () -> Unit,
    onNotificationPreferences: () -> Unit,
    onPreparedness: () -> Unit,
    onLogout: () -> Unit,
    onGenericSettings: (String) -> Unit
) {
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            AppHeader("Profile", actionIcon = Icons.Outlined.Settings, onAction = onSettings)
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 18.dp)) {
                    Box(
                        Modifier.size(112.dp).background(
                            androidx.compose.ui.graphics.Brush.linearGradient(listOf(DeepBlue, Teal)),
                            RoundedCornerShape(24.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.AccountCircle, null, tint = Color.White, modifier = Modifier.size(82.dp))
                    }
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(profile.fullName, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 25.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(profile.email, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(profile.district, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f), fontSize = 13.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    SettingsRow("Personal Information", Icons.Outlined.Person, onPersonalInformation)
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow(
                        "Preparedness Hub",
                        Icons.Outlined.Shield,
                        onPreparedness,
                        subtitle = "Checklist and nearest station"
                    )
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("Notification Preferences", Icons.Outlined.Notifications, onNotificationPreferences)
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("Help & Support", Icons.Outlined.HelpOutline, { onGenericSettings("Help & Support") })
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("About FloodSense", Icons.Outlined.Info, { onGenericSettings("About FloodSense") })
                }
                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onLogout),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Outlined.ExitToApp, null, tint = Color.Red)
                        Spacer(Modifier.width(10.dp))
                        Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onBack: () -> Unit,
    onPersonalInformation: () -> Unit,
    onNotificationPreferences: () -> Unit,
    onEmergencyContacts: () -> Unit,
    onPreparedness: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    onGenericSettings: (String) -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            AppHeader("Settings", onBack, Icons.Outlined.Settings) { }
            Column(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    SettingsRow("Account Settings", Icons.Outlined.AccountCircle, { onGenericSettings("Account Settings") })
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("Personal Information", Icons.Outlined.Person, onPersonalInformation)
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("Change Password", Icons.Outlined.Lock, { onGenericSettings("Change Password") })
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("Notification Preferences", Icons.Outlined.Notifications, onNotificationPreferences)
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow(
                        "Preparedness Hub",
                        Icons.Outlined.Shield,
                        onPreparedness,
                        subtitle = "Checklist and nearest station"
                    )
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("Emergency Contacts", Icons.Outlined.Contacts, onEmergencyContacts, tint = Color(0xFFF44336))
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("Location Settings", Icons.Outlined.LocationOn, { onGenericSettings("Location Settings") })
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("Language", Icons.Outlined.Language, { onGenericSettings("Language") }, subtitle = "English (US)")
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("Privacy & Security", Icons.Outlined.Security, { onGenericSettings("Privacy & Security") })
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow(
                        "App Theme",
                        Icons.Outlined.Palette,
                        { showThemeDialog = true },
                        subtitle = themeMode.name.lowercase().replaceFirstChar { it.uppercase() }
                    )
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("Help & Support", Icons.Outlined.HelpOutline, { onGenericSettings("Help & Support") })
                    HorizontalDivider(color = PaleBlue)
                    SettingsRow("About FloodSense", Icons.Outlined.Info, { onGenericSettings("About FloodSense") })
                }
                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onLogout),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Outlined.ExitToApp, null, tint = Color.Red)
                        Spacer(Modifier.width(10.dp))
                        Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose App Theme", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                onThemeChange(mode)
                                showThemeDialog = false
                            }.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }, modifier = Modifier.weight(1f))
                            if (mode == themeMode) Text("✓", color = Teal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
fun PreparednessScreen(
    items: List<PreparednessItem>,
    districtName: String,
    stations: List<Station>,
    selectedStationId: String?,
    onBack: () -> Unit,
    onToggleItem: (String, Boolean) -> Unit,
    onSelectStation: (Station) -> Unit,
    onSelectNearestStation: (Double, Double) -> Station?,
    onEmergencyContacts: () -> Unit
) {
    val context = LocalContext.current
    var nearestMessage by remember { mutableStateOf<String?>(null) }
    val requestLocation = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) {
            nearestMessage = applyNearestStation(context, onSelectNearestStation)
        } else {
            nearestMessage = "Allow location to select the nearest Kalu Ganga station."
        }
    }
    val complete = if (items.isEmpty()) 0f else items.count { it.completed }.toFloat() / items.size
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground(strong = true)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, "Back", tint = DeepBlue) }
                FloodSenseLogo(modifier = Modifier.weight(1f))
                Spacer(Modifier.size(48.dp))
            }
            Column(Modifier.padding(horizontal = 20.dp)) {
                Spacer(Modifier.height(8.dp))
                Text("Preparedness Hub", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, tint = Teal)
                    Spacer(Modifier.width(8.dp))
                    Text(districtName, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
                Text("Tick each item as you complete it, then choose the gauge you will follow.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f), lineHeight = 22.sp)
                Spacer(Modifier.height(22.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Family Safety Plan", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            Spacer(Modifier.height(10.dp))
                            Text("Work through the checklist so your household is ready before water rises.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = complete,
                                modifier = Modifier.fillMaxSize(),
                                color = Teal,
                                trackColor = PaleBlue,
                                strokeWidth = 9.dp
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(complete * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 25.sp)
                                Text("Complete", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Readiness Checklist", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Select each step as you finish it.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(14.dp))
                        items.forEachIndexed { index, item ->
                            val icon = when (item.id) {
                                "kit" -> Icons.Outlined.MedicalServices
                                "route" -> Icons.Outlined.Directions
                                "contacts" -> Icons.Outlined.Contacts
                                "insurance" -> Icons.Outlined.Shield
                                "station" -> Icons.Outlined.LocationOn
                                else -> Icons.Outlined.Home
                            }
                            ChecklistRow(
                                title = item.title,
                                completed = item.completed,
                                onToggle = { onToggleItem(item.id, !item.completed) },
                                icon = icon,
                                subtitle = item.description
                            )
                            if (index != items.lastIndex) HorizontalDivider(color = PaleBlue)
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Your station", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Select the Kalu Ganga gauge you want FloodSense to follow.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        NearestStationButton(
                            onClick = {
                                if (DeviceLocation.hasPermission(context)) {
                                    nearestMessage = applyNearestStation(context, onSelectNearestStation)
                                } else {
                                    requestLocation.launch(DeviceLocation.permissions)
                                }
                            }
                        )
                        nearestMessage?.let { message ->
                            Spacer(Modifier.height(8.dp))
                            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(10.dp))
                        stations.forEachIndexed { index, station ->
                            val selected = station.id == selectedStationId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectStation(station)
                                        nearestMessage = "Following ${station.name}."
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = {
                                        onSelectStation(station)
                                        nearestMessage = "Following ${station.name}."
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Teal)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(station.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "${station.district} · ${"%.2f".format(station.waterLevel)} m",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (index != stations.lastIndex) HorizontalDivider(color = PaleBlue)
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onEmergencyContacts),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(58.dp).background(PaleBlue, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Contacts, null, tint = DeepBlue)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Emergency Contacts", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Quick access to important contacts when you need them.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                        Text("›", color = DeepBlue, fontSize = 34.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PaleBlue.copy(alpha = 0.86f))
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Stay prepared. Stay safe.", color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("FloodSense helps you plan, prepare, and protect.", color = TextSecondary)
                        }
                        Icon(Icons.Outlined.WaterDrop, null, tint = Teal, modifier = Modifier.size(50.dp))
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

private fun applyNearestStation(
    context: android.content.Context,
    onSelectNearestStation: (Double, Double) -> Station?
): String {
    val location = DeviceLocation.lastKnown(context)
        ?: return "No GPS fix yet. Choose a station from the list, or try again outdoors."
    val station = onSelectNearestStation(location.latitude, location.longitude)
        ?: return "No Kalu Ganga station is available to select."
    return "Nearest station: ${station.name}."
}

@Composable
private fun NearestStationButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape, ambientColor = DeepBlue.copy(alpha = 0.32f), spotColor = Teal.copy(alpha = 0.45f))
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Navy, DeepBlue, BrightBlue, Cyan, Teal)
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.28f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent)
                    )
                )
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Select nearest station",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Uses GPS to follow the closest Kalu Ganga gauge",
                    color = Color.White.copy(alpha = 0.86f),
                    fontSize = 12.sp
                )
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
fun PersonalInformationScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onSave: (UserProfile) -> Unit,
    darkHeader: Boolean = false
) {
    var name by remember(profile) { mutableStateOf(profile.fullName) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var phone by remember(profile) { mutableStateOf(profile.phone) }
    var district by remember(profile) { mutableStateOf(SriLankaDistricts.resolveHome(profile.district).name) }
    var saved by remember { mutableStateOf(false) }
    var pickingDistrict by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .then(if (darkHeader) Modifier.background(SurfaceBlue) else Modifier.statusBarsPadding())
    ) {
        if (!darkHeader) WaveBackground()
        Column(Modifier.fillMaxSize()) {
        if (darkHeader) {
            AdminHeader("Personal Information", onBack)
        } else {
            AppHeader("Personal Information", onBack, Icons.Outlined.Edit) { }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(110.dp).background(PaleBlue, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.AccountCircle, null, tint = DeepBlue, modifier = Modifier.size(84.dp))
            }
            Spacer(Modifier.height(24.dp))
            FloodFormSheet {
            ProfileTextField("Full Name", name, { name = it }, Icons.Outlined.Person)
            Spacer(Modifier.height(16.dp))
            ProfileTextField("Email Address", email, { email = it }, Icons.Outlined.AccountCircle, KeyboardType.Email)
            Spacer(Modifier.height(16.dp))
            ProfileTextField("Contact Number", phone, { phone = it }, Icons.Outlined.Phone, KeyboardType.Phone)
            Spacer(Modifier.height(16.dp))
            Text(
                "District",
                color = Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp, start = 2.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pickingDistrict = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.LocationOn, null, tint = TextSecondary)
                    Spacer(Modifier.width(12.dp))
                    Text(district, color = Navy, modifier = Modifier.weight(1f), fontSize = 16.sp)
                    Icon(Icons.Outlined.KeyboardArrowDown, null, tint = TextSecondary)
                }
            }
            Spacer(Modifier.height(20.dp))
            PrimaryGradientButton(
                "Save Changes",
                {
                    onSave(UserProfile(name, email, phone, district))
                    saved = true
                },
                Modifier.fillMaxWidth()
            )
            }
            if (saved) {
                Spacer(Modifier.height(12.dp))
                Text("Profile updated.", color = Teal, fontWeight = FontWeight.SemiBold)
            }
        }
        }
        if (pickingDistrict) {
            DistrictPickerDialog(
                districts = SriLankaDistricts.home,
                selectedName = district,
                onSelect = {
                    district = it.name
                    pickingDistrict = false
                },
                onDismiss = { pickingDistrict = false }
            )
        }
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    FloodField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        placeholder = label,
        leadingIcon = { Icon(icon, null) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun NotificationPreferencesScreen(
    settings: NotificationSettings,
    onBack: () -> Unit,
    onUpdate: (NotificationSettings) -> Unit
) {
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(Modifier.fillMaxSize()) {
        AppHeader("Notifications", onBack)
        Column(Modifier.padding(20.dp)) {
            Text("Flood warnings", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(14.dp))
            NotificationSwitchRow("Critical flood alerts", "Immediate high-risk warnings", settings.criticalAlerts) {
                onUpdate(settings.copy(criticalAlerts = it))
            }
            NotificationSwitchRow("Rising water alerts", "Warnings when levels increase quickly", settings.risingWaterAlerts) {
                onUpdate(settings.copy(risingWaterAlerts = it))
            }
            NotificationSwitchRow("Heavy rainfall alerts", "Rainfall intensity warnings", settings.rainfallAlerts) {
                onUpdate(settings.copy(rainfallAlerts = it))
            }
            NotificationSwitchRow("Station offline alerts", "Notify when a sensor station disconnects", settings.stationOfflineAlerts) {
                onUpdate(settings.copy(stationOfflineAlerts = it))
            }
            Spacer(Modifier.height(24.dp))
            Text("Alert behaviour", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(14.dp))
            NotificationSwitchRow("Vibration", "Vibrate for important alerts", settings.vibration) {
                onUpdate(settings.copy(vibration = it))
            }
        }
        }
    }
}

@Composable
private fun NotificationSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Teal)
            )
        }
    }
}

@Composable
fun EmergencyContactsScreen(
    contacts: List<EmergencyContact>,
    onBack: () -> Unit,
    onAdd: (EmergencyContact) -> Unit,
    onRemove: (EmergencyContact) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(Modifier.fillMaxSize()) {
        AppHeader("Emergency Contacts", onBack, Icons.Outlined.Add) { showAdd = true }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text(
                "Add a Sri Lankan mobile (+94…). Emergency SMS is sent only on HIGH flood alerts, and only two demo texts are sent in total. FCM still notifies this phone. 117 and other short codes are not texted.",
                color = TextSecondary,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(16.dp))
            contacts.forEach { contact ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(50.dp).background(PaleBlue, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Contacts, null, tint = DeepBlue)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(contact.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(contact.relationship, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(contact.phone, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onRemove(contact) }) {
                            Icon(Icons.Outlined.Delete, "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        }
        }
    }
    if (showAdd) {
        AddContactDialog(onDismiss = { showAdd = false }) {
            onAdd(it)
            showAdd = false
        }
    }
}

@Composable
private fun AddContactDialog(onDismiss: () -> Unit, onAdd: (EmergencyContact) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Emergency Contact", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                FloodField(label = "Name", value = name, onValueChange = { name = it }, placeholder = "Contact name")
                Spacer(Modifier.height(10.dp))
                FloodField(
                    label = "Phone",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "07XXXXXXXX",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Spacer(Modifier.height(10.dp))
                FloodField(label = "Relationship", value = relation, onValueChange = { relation = it }, placeholder = "Family, neighbor...")
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank() && phone.isNotBlank()) onAdd(EmergencyContact(name, phone, relation.ifBlank { "Contact" })) }) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun GenericSettingsDetailScreen(title: String, onBack: () -> Unit) {
    val content = when (title) {
        "Account Settings" -> "Manage your FloodSense account, linked sign-in methods, and account status."
        "Change Password" -> "Password changes will be connected to Firebase Authentication in the live integration phase."
        "Location Settings" -> "Choose the district and sensor stations used for local flood warnings."
        "Language" -> "English is enabled in this project. Sinhala and Tamil resources can be added using Android string resource folders."
        "Privacy & Security" -> "FloodSense only needs network, notification, and optional location access for risk alerts and map features."
        "Help & Support" -> "For the university demo, use this screen to display troubleshooting steps, emergency guidance, and project contact information."
        "About FloodSense" -> "FloodSense is a hybrid AI, machine-learning and IoT flood detection system designed for real-time monitoring and early warning in Sri Lanka."
        else -> "This page is ready for its live backend integration."
    }
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(Modifier.fillMaxSize()) {
        AppHeader(title, onBack)
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            FloodSenseLogo()
            Spacer(Modifier.height(28.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Spacer(Modifier.height(14.dp))
                    Text(content, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 24.sp)
                }
            }
        }
        }
    }
}
