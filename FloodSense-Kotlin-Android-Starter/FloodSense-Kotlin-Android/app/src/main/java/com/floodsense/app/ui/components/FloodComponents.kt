package com.floodsense.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import android.app.Activity
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.floodsense.app.ui.theme.AdminNavy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floodsense.app.R
import com.floodsense.app.data.RiskLevel
import com.floodsense.app.navigation.Screen
import com.floodsense.app.ui.theme.BrightBlue
import com.floodsense.app.ui.theme.CardHairline
import com.floodsense.app.ui.theme.Cyan
import com.floodsense.app.ui.theme.Danger
import com.floodsense.app.ui.theme.DeepBlue
import com.floodsense.app.ui.theme.FieldStroke
import com.floodsense.app.ui.theme.FieldStrokeFocus
import com.floodsense.app.ui.theme.Navy
import com.floodsense.app.ui.theme.PaleBlue
import com.floodsense.app.ui.theme.Teal
import com.floodsense.app.ui.theme.TextSecondary

@Composable
fun WaveBackground(modifier: Modifier = Modifier, strong: Boolean = false) {
    val background = MaterialTheme.colorScheme.background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(if (strong) 0.42f else 0.34f)
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            alpha = 0.88f
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(if (strong) 0.72f else 0.78f)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            background,
                            background,
                            background.copy(alpha = 0.88f),
                            background.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun FloodField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val stroke = when {
        isError -> Danger
        focused -> FieldStrokeFocus
        else -> FieldStroke
    }
    Column(modifier.fillMaxWidth()) {
        if (label.isNotBlank()) {
            Text(
                label,
                color = Navy,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 10.dp, start = 2.dp)
            )
        }
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = if (focused) 10.dp else 6.dp),
            border = BorderStroke(if (focused || isError) 2.dp else 1.5.dp, stroke)
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    if (placeholder.isNotBlank()) {
                        Text(placeholder, color = TextSecondary.copy(alpha = 0.85f))
                    }
                },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                modifier = Modifier.fillMaxWidth(),
                singleLine = singleLine,
                minLines = minLines,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                isError = isError,
                readOnly = readOnly,
                interactionSource = interaction,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Navy,
                    unfocusedTextColor = Navy,
                    disabledTextColor = Navy.copy(alpha = 0.60f),
                    errorTextColor = Navy,

                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    errorContainerColor = Color.White,

                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,

                    focusedLeadingIconColor = Teal,
                    unfocusedLeadingIconColor = TextSecondary,
                    disabledLeadingIconColor = TextSecondary.copy(alpha = 0.60f),
                    errorLeadingIconColor = Danger,

                    focusedTrailingIconColor = DeepBlue,
                    unfocusedTrailingIconColor = TextSecondary,
                    disabledTrailingIconColor = TextSecondary.copy(alpha = 0.60f),
                    errorTrailingIconColor = Danger,

                    focusedPlaceholderColor = TextSecondary.copy(alpha = 0.85f),
                    unfocusedPlaceholderColor = TextSecondary.copy(alpha = 0.85f),
                    disabledPlaceholderColor = TextSecondary.copy(alpha = 0.60f),
                    errorPlaceholderColor = TextSecondary.copy(alpha = 0.85f),

                    cursorColor = Teal,
                    errorCursorColor = Danger
                )
            )
        }
    }
}

@Composable
fun FloodFormSheet(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        border = BorderStroke(1.dp, CardHairline)
    ) {
        Column(Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun ScreenBackdrop(
    strong: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground(strong = strong)
        content()
    }
}

@Composable
fun FloodSenseLogo(
    modifier: Modifier = Modifier,
    showWordmark: Boolean = true,
    large: Boolean = false,
    light: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "FloodSense",
            modifier = Modifier.size(if (large) 88.dp else 48.dp),
            contentScale = ContentScale.Fit
        )
        if (showWordmark) {
            Spacer(Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Flood",
                    color = if (light) Color.White else Navy,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (large) 34.sp else 25.sp
                )
                Text(
                    text = "Sense",
                    color = if (light) Cyan else Teal,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (large) 34.sp else 25.sp
                )
            }
        }
    }
}

@Composable
fun AdminLogo(modifier: Modifier = Modifier, size: Dp = 42.dp) {
    Image(
        painter = painterResource(R.drawable.admin_logo),
        contentDescription = "FloodSense",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun AdminBrandMark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AdminLogo(size = 44.dp)
        Spacer(Modifier.width(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Flood", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("Sense", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
    }
}

@Composable
fun AdminStatusBar() {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        window.statusBarColor = AdminNavy.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }
}

@Composable
fun AdminHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null
) {
    AdminStatusBar()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AdminNavy)
            .statusBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
            AdminLogo(size = 32.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        subtitle,
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (actionIcon != null && onAction != null) {
                IconButton(onClick = onAction) {
                    Icon(actionIcon, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun AdminHeroHeader(
    title: String,
    subtitle: String? = null,
    extra: @Composable ColumnScope.() -> Unit = {}
) {
    AdminStatusBar()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AdminNavy)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 24.dp)
    ) {
        AdminBrandMark()
        Spacer(Modifier.height(18.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 20.sp)
        }
        extra()
    }
}

@Composable
fun AppHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Navy)
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (actionIcon != null && onAction != null) {
            IconButton(onClick = onAction) {
                Icon(actionIcon, contentDescription = null, tint = DeepBlue)
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

@Composable
fun PrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val alpha = if (enabled) 1f else 0.45f
    Box(
        modifier = modifier
            .height(56.dp)
            .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = DeepBlue.copy(alpha = 0.28f), spotColor = Teal.copy(alpha = 0.35f))
            .background(
                brush = Brush.horizontalGradient(listOf(DeepBlue.copy(alpha = alpha), Teal.copy(alpha = alpha))),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
            }
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun RiskBadge(level: RiskLevel, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = level.color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, level.color.copy(alpha = 0.25f))
    ) {
        Text(
            text = "${level.label} Risk",
            color = level.color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = BrightBlue
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, CardHairline)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                Modifier
                    .size(46.dp)
                    .background(iconTint.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(title, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Text(value, color = Navy, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(Modifier.height(10.dp))
            Text(subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
fun CitizenSelectField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, CardHairline)
        ) {
            Row(
                Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, null, tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        value,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!supporting.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(supporting, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
                Icon(Icons.Outlined.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tint: Color = DeepBlue,
    trailingText: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(18.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(Modifier.height(6.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
        if (trailingText != null) {
            Text(trailingText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = DeepBlue)
    }
}

@Composable
fun ChecklistRow(
    title: String,
    completed: Boolean,
    onToggle: () -> Unit,
    icon: ImageVector = Icons.Outlined.Home,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(PaleBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = DeepBlue)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
        Checkbox(
            checked = completed,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(checkedColor = Teal, uncheckedColor = TextSecondary)
        )
    }
}

data class BottomItem(val route: String, val label: String, val icon: ImageVector)

val bottomItems = listOf(
    BottomItem(Screen.Home.route, "Home", Icons.Outlined.Home),
    BottomItem(Screen.Prediction.route, "Prediction", Icons.Outlined.ShowChart),
    BottomItem(Screen.Alerts.route, "Alerts", Icons.Outlined.Notifications),
    BottomItem(Screen.Profile.route, "Profile", Icons.Outlined.Person)
)

val adminBottomItems = listOf(
    BottomItem(Screen.AdminHome.route, "Dashboard", Icons.Outlined.Home),
    BottomItem(Screen.AdminStations.route, "Stations", Icons.Outlined.LocationOn),
    BottomItem(Screen.AdminAlerts.route, "Alerts", Icons.Outlined.Notifications),
    BottomItem(Screen.AdminUsers.route, "Users", Icons.Outlined.Person),
    BottomItem(Screen.AdminMore.route, "More", Icons.Outlined.MoreHoriz)
)

@Composable
fun FloodBottomBar(
    currentRoute: String?,
    items: List<BottomItem> = bottomItems,
    onNavigate: (String) -> Unit
) {
    val admin = items.any { it.route.startsWith("admin") }
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (admin) AdminNavy else Teal,
                    selectedTextColor = if (admin) AdminNavy else Teal,
                    indicatorColor = PaleBlue,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

@Composable
fun SensorIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val centerX = size.width * 0.52f
            drawLine(
                color = Navy,
                start = Offset(centerX, size.height * 0.25f),
                end = Offset(centerX, size.height * 0.72f),
                strokeWidth = 9f,
                cap = StrokeCap.Round
            )
            drawCircle(color = DeepBlue, radius = 23f, center = Offset(centerX, size.height * 0.22f))
            drawCircle(color = Cyan, radius = 12f, center = Offset(centerX, size.height * 0.22f))
            drawRect(
                color = DeepBlue,
                topLeft = Offset(centerX - 36f, size.height * 0.47f),
                size = androidx.compose.ui.geometry.Size(72f, 54f)
            )
            repeat(3) { index ->
                drawArc(
                    color = Teal.copy(alpha = 1f - index * 0.2f),
                    startAngle = -55f,
                    sweepAngle = 110f,
                    useCenter = false,
                    topLeft = Offset(centerX + 18f + index * 10f, size.height * 0.14f - index * 10f),
                    size = androidx.compose.ui.geometry.Size(35f + index * 20f, 55f + index * 20f),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }
        }
    }
}
