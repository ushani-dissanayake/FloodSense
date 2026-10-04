package com.floodsense.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floodsense.app.R
import com.floodsense.app.ui.components.FloodField
import com.floodsense.app.ui.components.FloodFormSheet
import com.floodsense.app.ui.components.FloodSenseLogo
import com.floodsense.app.ui.components.PrimaryGradientButton
import com.floodsense.app.ui.components.WaveBackground
import com.floodsense.app.ui.theme.DeepBlue
import com.floodsense.app.ui.theme.Navy
import com.floodsense.app.ui.theme.Teal
import com.floodsense.app.ui.theme.Success
import com.floodsense.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(ready: Boolean = true, onFinished: () -> Unit) {
    LaunchedEffect(ready) {
        if (!ready) return@LaunchedEffect
        delay(2200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.54f)
                .align(Alignment.BottomCenter)
        ) {
            Image(
                painter = painterResource(R.drawable.splash_city),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomCenter
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.38f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color.White.copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "FloodSense",
                modifier = Modifier.size(118.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "FloodSense",
                color = Teal,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "INTELLIGENT FLOOD DETECTION\n& EARLY WARNING SYSTEM",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                letterSpacing = 1.1.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = "www.floodsense.app",
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 14.dp)
        )
    }
}

@Composable
fun LoginScreen(
    busy: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }
    val valid = email.contains("@") && password.length >= 6

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
    ) {
        WaveBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloodSenseLogo()
            Spacer(Modifier.height(8.dp))
            Text("Sign in to continue monitoring", color = TextSecondary, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Box(Modifier.size(width = 56.dp, height = 4.dp).background(Teal, CircleShape))
            Spacer(Modifier.height(28.dp))
            FloodFormSheet {
            FloodTextField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                placeholder = "Enter your email",
                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = attempted && !email.contains("@")
            )
            Spacer(Modifier.height(18.dp))
            FloodTextField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "Enter your password",
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null)
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = attempted && password.length < 6
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Forgot Password?",
                    color = DeepBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable(onClick = onForgotPassword)
                        .padding(vertical = 12.dp)
                )
            }
            PrimaryGradientButton(
                text = if (busy) "Signing in..." else "Login",
                onClick = {
                    attempted = true
                    if (valid && !busy) onLogin(email.trim(), password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy
            )
            }
            if (attempted && !valid) {
                Spacer(Modifier.height(8.dp))
                Text("Use a valid email and a password with at least 6 characters.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            if (!error.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            Spacer(Modifier.height(28.dp))
            Row {
                Text("Don't have an account? ", color = Navy)
                Text("Sign Up", color = DeepBlue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onRegister))
            }
            if (onBack != null) {
                Spacer(Modifier.height(18.dp))
                Text("Back to role selection", color = DeepBlue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
            }
            Spacer(Modifier.height(130.dp))
        }
    }
}

@Composable
fun RegisterScreen(
    busy: Boolean,
    error: String?,
    onRegister: (String, String, String) -> Unit,
    onLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var accepted by remember { mutableStateOf(true) }
    var attempted by remember { mutableStateOf(false) }
    val valid = name.length >= 3 && email.contains("@") && password.length >= 6 && password == confirmPassword && accepted

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
    ) {
        WaveBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            FloodSenseLogo()
            Spacer(Modifier.height(30.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineLarge, color = Navy)
            Text("Sign up to get started", color = TextSecondary, fontSize = 18.sp)
            Spacer(Modifier.height(22.dp))
            FloodFormSheet {
            FloodTextField(
                label = "Full Name",
                value = name,
                onValueChange = { name = it },
                placeholder = "Enter your name",
                leadingIcon = { Icon(Icons.Outlined.AccountCircle, null) },
                isError = attempted && name.length < 3
            )
            Spacer(Modifier.height(18.dp))
            FloodTextField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                placeholder = "Enter your email",
                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = attempted && !email.contains("@")
            )
            Spacer(Modifier.height(18.dp))
            FloodTextField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "Create password",
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null)
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = attempted && password.length < 6
            )
            Spacer(Modifier.height(18.dp))
            FloodTextField(
                label = "Confirm Password",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm password",
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                isError = attempted && password != confirmPassword
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                Checkbox(
                    checked = accepted,
                    onCheckedChange = { accepted = it },
                    colors = CheckboxDefaults.colors(checkedColor = Teal)
                )
                Text("I agree to the ", color = Navy)
                Text("Terms & Conditions", color = DeepBlue, fontWeight = FontWeight.SemiBold)
            }
            }
            Spacer(Modifier.height(16.dp))
            PrimaryGradientButton(
                text = if (busy) "Creating account..." else "Sign Up",
                onClick = {
                    attempted = true
                    if (valid && !busy) onRegister(name.trim(), email.trim(), password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy
            )
            if (attempted && !valid) {
                Spacer(Modifier.height(8.dp))
                Text("Please complete all fields correctly.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            if (!error.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            Spacer(Modifier.height(26.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Already have an account? ", color = Navy)
                Text("Login", color = DeepBlue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onLogin))
            }
            Spacer(Modifier.height(120.dp))
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    busy: Boolean,
    message: String?,
    error: String?,
    onSend: (String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FloodSenseLogo()
            Spacer(Modifier.height(32.dp))
            Text("Reset Password", style = MaterialTheme.typography.headlineMedium, color = Navy)
            Spacer(Modifier.height(8.dp))
            Text("Enter your email and we will send reset instructions.", color = TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            FloodFormSheet {
            FloodTextField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                placeholder = "Enter your email",
                leadingIcon = { Icon(Icons.Outlined.Email, null) }
            )
            Spacer(Modifier.height(22.dp))
            PrimaryGradientButton(
                if (busy) "Sending..." else "Send Reset Link",
                { if (email.contains("@") && !busy) onSend(email.trim()) },
                Modifier.fillMaxWidth(),
                enabled = !busy
            )
            }
            if (!message.isNullOrBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(message, color = Teal, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }
            if (!error.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(18.dp))
            Text("Back to Login", color = DeepBlue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
        }
    }
}

@Composable
fun RoleSelectScreen(
    onCitizen: () -> Unit,
    onAdmin: () -> Unit
) {
    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground(strong = true)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FloodSenseLogo(large = true)
            Spacer(Modifier.height(16.dp))
            Text("Smart Flood Detection\n& Early Warning System", color = Navy, fontSize = 20.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("Choose how you enter FloodSense", color = TextSecondary, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            RoleCard(
                title = "Citizen Login",
                subtitle = "View stations, risk, alerts, and preparedness",
                onClick = onCitizen
            )
            Spacer(Modifier.height(14.dp))
            RoleCard(
                title = "Administrator",
                subtitle = "Manage stations, users, and alerts",
                onClick = onAdmin
            )
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
fun AdminLoginScreen(
    busy: Boolean,
    error: String?,
    message: String?,
    onSignIn: (email: String, password: String, createAccount: Boolean, name: String) -> Unit,
    onBack: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var createAccount by remember { mutableStateOf(false) }
    var attempted by remember { mutableStateOf(false) }
    val canSignIn = email.contains("@") && password.length >= 6 && (!createAccount || name.trim().length >= 2)

    Box(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
        WaveBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloodSenseLogo()
            Spacer(Modifier.height(16.dp))
            Text("Administrator Login", style = MaterialTheme.typography.headlineMedium, color = Navy)
            Spacer(Modifier.height(8.dp))
            Text("Sign in with email and password", color = Success, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(20.dp))
            FloodFormSheet {
            if (createAccount) {
                FloodTextField(
                    label = "Full Name",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Administrator name",
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, null) }
                )
                Spacer(Modifier.height(16.dp))
            }
            FloodTextField(
                label = "Admin Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "admin@email.com",
                leadingIcon = { Icon(Icons.Outlined.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Spacer(Modifier.height(16.dp))
            FloodTextField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "Enter password",
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null)
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(createAccount, { createAccount = it }, colors = CheckboxDefaults.colors(checkedColor = Teal))
                Text("Create admin account", color = Navy, modifier = Modifier.weight(1f))
                Text("Forgot Password?", color = DeepBlue, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onForgotPassword))
            }
            PrimaryGradientButton(
                text = when {
                    busy -> "Please wait..."
                    createAccount -> "Create account and sign in"
                    else -> "Sign in"
                },
                onClick = {
                    attempted = true
                    if (canSignIn && !busy) onSignIn(email.trim(), password, createAccount, name.trim())
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy
            )
            }
            if (attempted && !canSignIn) {
                Spacer(Modifier.height(8.dp))
                Text("Enter a valid email and a password with at least 6 characters.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            if (!message.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(message, color = Teal, fontSize = 12.sp)
            }
            if (!error.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("Authorized personnel only", color = TextSecondary)
            Spacer(Modifier.height(20.dp))
            Text("Back", color = DeepBlue, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
        }
    }
}

@Composable
private fun FloodTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false
) {
    FloodField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        isError = isError
    )
}
