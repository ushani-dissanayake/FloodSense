package com.floodsense.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.floodsense.app.data.AppRole
import com.floodsense.app.data.DistrictOverview
import com.floodsense.app.data.SriLankaDistricts
import com.floodsense.app.ui.AuthStatus
import com.floodsense.app.navigation.Screen
import com.floodsense.app.ui.components.FloodBottomBar
import com.floodsense.app.ui.components.adminBottomItems
import com.floodsense.app.ui.components.bottomItems
import com.floodsense.app.ui.screens.AdminAddStationScreen
import com.floodsense.app.ui.screens.AdminAddUserScreen
import com.floodsense.app.ui.screens.AdminAlertDetailsScreen
import com.floodsense.app.ui.screens.AdminAlertsScreen
import com.floodsense.app.ui.screens.AdminCreateAlertScreen
import com.floodsense.app.ui.screens.AdminDashboardScreen
import com.floodsense.app.ui.screens.AdminEmergencyNotificationsScreen
import com.floodsense.app.ui.screens.AdminHistoryScreen
import com.floodsense.app.ui.screens.AdminLoginScreen
import com.floodsense.app.ui.screens.AdminMoreScreen
import com.floodsense.app.ui.screens.AdminPredictionAnalyticsScreen
import com.floodsense.app.ui.screens.AdminReportsScreen
import com.floodsense.app.ui.screens.AdminSettingsScreen
import com.floodsense.app.ui.screens.AdminStationDetailsScreen
import com.floodsense.app.ui.screens.AdminStationsScreen
import com.floodsense.app.ui.screens.AdminUsersScreen
import com.floodsense.app.ui.screens.OnboardingScreen
import com.floodsense.app.ui.screens.AlertDetailsScreen
import com.floodsense.app.ui.screens.AlertsScreen
import com.floodsense.app.ui.screens.DashboardScreen
import com.floodsense.app.ui.screens.EmergencyContactsScreen
import com.floodsense.app.ui.screens.ForgotPasswordScreen
import com.floodsense.app.ui.screens.GenericSettingsDetailScreen
import com.floodsense.app.ui.screens.HistoryScreen
import com.floodsense.app.ui.screens.LoginScreen
import com.floodsense.app.ui.screens.NotificationPreferencesScreen
import com.floodsense.app.ui.screens.PersonalInformationScreen
import com.floodsense.app.ui.screens.PredictionScreen
import com.floodsense.app.ui.screens.PreparednessScreen
import com.floodsense.app.ui.screens.ProfileScreen
import com.floodsense.app.ui.screens.RegisterScreen
import com.floodsense.app.ui.screens.RiskMapScreen
import com.floodsense.app.ui.screens.RoleSelectScreen
import com.floodsense.app.ui.screens.SettingsScreen
import com.floodsense.app.ui.screens.SplashScreen
import com.floodsense.app.ui.screens.StationDetailsScreen
import com.floodsense.app.ui.screens.StationsScreen

@Composable
fun FloodSenseApp(viewModel: FloodSenseViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val role by viewModel.role.collectAsState()
    val authStatus by viewModel.authStatus.collectAsState()
    val authBusy by viewModel.authBusy.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authMessage by viewModel.authMessage.collectAsState()
    val navItems = if (role == AppRole.ADMIN) adminBottomItems else bottomItems
    val homeRoute = if (role == AppRole.ADMIN) Screen.AdminHome.route else Screen.Home.route
    val hideBottomBar = currentRoute == null || currentRoute in setOf(
        Screen.Splash.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.ForgotPassword.route,
        Screen.RoleSelect.route,
        Screen.Onboarding.route,
        Screen.AdminLogin.route
    )

    fun goHome() {
        navController.navigate(homeRoute) {
            popUpTo(homeRoute) {
                inclusive = false
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }

    val stations by viewModel.stations.collectAsState()
    val readings by viewModel.readings.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val selectedStationId by viewModel.selectedStation.collectAsState()
    val selectedStation = selectedStationId?.let { selected ->
        stations.firstOrNull { it.id == selected.id } ?: selected
    }
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val homeStationId by viewModel.homeStationId.collectAsState()
    val homeStations = remember(stations) { DistrictOverview.allHomeStations(stations) }
    val homeStation = remember(selectedDistrict, homeStationId, stations, selectedStation) {
        selectedStation?.takeIf { station -> homeStations.any { it.id == station.id } }
            ?: DistrictOverview.station(selectedDistrict, stations, homeStationId)
    }
    val selectedAlert by viewModel.selectedAlert.collectAsState()
    val selectedAdminAlert by viewModel.selectedAdminAlert.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val preparednessItems by viewModel.preparednessItems.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val notificationSettings by viewModel.notificationSettings.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()
    val settingsTitle by viewModel.selectedSettingsTitle.collectAsState()
    val managedUsers by viewModel.managedUsers.collectAsState()
    val adminAlerts by viewModel.adminAlerts.collectAsState()

    fun logout() {
        viewModel.signOut()
        navController.navigate(Screen.RoleSelect.route) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        bottomBar = {
            if (!hideBottomBar && (role == AppRole.USER || role == AppRole.ADMIN)) {
                FloodBottomBar(currentRoute = currentRoute, items = navItems) { route ->
                    if (route == homeRoute) {
                        goHome()
                    } else {
                        navController.navigate(route) {
                            popUpTo(homeRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(ready = authStatus != AuthStatus.Checking) {
                    val destination = when (authStatus) {
                        AuthStatus.Citizen -> Screen.Home.route
                        AuthStatus.Admin -> Screen.AdminHome.route
                        else -> Screen.Onboarding.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.RoleSelect.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            }
            composable(Screen.RoleSelect.route) {
                RoleSelectScreen(
                    onCitizen = {
                        viewModel.clearAuthFeedback()
                        viewModel.setRole(AppRole.USER)
                        navController.navigate(Screen.Login.route)
                    },
                    onAdmin = {
                        viewModel.clearAuthFeedback()
                        viewModel.setRole(AppRole.ADMIN)
                        navController.navigate(Screen.AdminLogin.route)
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    busy = authBusy,
                    error = authError,
                    onLogin = { email, password ->
                        viewModel.signInCitizen(email, password) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.RoleSelect.route) { inclusive = true }
                            }
                        }
                    },
                    onRegister = {
                        viewModel.clearAuthFeedback()
                        navController.navigate(Screen.Register.route)
                    },
                    onForgotPassword = {
                        viewModel.clearAuthFeedback()
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminLogin.route) {
                AdminLoginScreen(
                    busy = authBusy,
                    error = authError,
                    message = authMessage,
                    onSignIn = { email, password, createAccount, name ->
                        viewModel.signInAdmin(
                            email = email,
                            password = password,
                            createIfMissing = createAccount,
                            name = name
                        ) {
                            navController.navigate(Screen.AdminHome.route) {
                                popUpTo(Screen.RoleSelect.route) { inclusive = true }
                            }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onForgotPassword = {
                        viewModel.clearAuthFeedback()
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    busy = authBusy,
                    error = authError,
                    onRegister = { name, email, password ->
                        viewModel.registerCitizen(name, email, password) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.RoleSelect.route) { inclusive = true }
                            }
                        }
                    },
                    onLogin = { navController.popBackStack() }
                )
            }
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    busy = authBusy,
                    message = authMessage,
                    error = authError,
                    onSend = viewModel::sendPasswordReset,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Home.route) {
                val district = SriLankaDistricts.resolveHome(selectedDistrict)
                val districtStations = remember(selectedDistrict, stations) {
                    DistrictOverview.stationsFor(selectedDistrict, stations)
                }
                val districtStation = remember(selectedDistrict, homeStationId, stations) {
                    DistrictOverview.station(selectedDistrict, stations, homeStationId)
                }
                val districtLog = remember(selectedDistrict, homeStationId) {
                    DistrictOverview.fieldLog(selectedDistrict, homeStationId)
                }
                val districtAlerts = remember(selectedDistrict, alerts, districtStation) {
                    DistrictOverview.alerts(selectedDistrict, alerts, districtStation)
                }
                DashboardScreen(
                    selectedDistrict = district.name,
                    districts = SriLankaDistricts.home,
                    districtStations = districtStations,
                    station = districtStation,
                    fieldLog = districtLog,
                    alerts = districtAlerts,
                    onSelectDistrict = viewModel::selectDistrict,
                    onSelectStation = { station ->
                        viewModel.selectHomeStation(station.id)
                        viewModel.selectStation(station)
                    },
                    onStationClick = { station ->
                        viewModel.selectStation(station)
                        navController.navigate(Screen.StationDetails.route)
                    },
                    onPredictionClick = {
                        (districtStation ?: homeStation)?.let(viewModel::selectStation)
                        navController.navigate(Screen.Prediction.route)
                    },
                    onAlertsClick = { navController.navigate(Screen.Alerts.route) },
                    onMapClick = { navController.navigate(Screen.Map.route) },
                    onPreparednessClick = { navController.navigate(Screen.Preparedness.route) },
                    onProfileClick = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.Stations.route) {
                val districtStation = remember(selectedDistrict, homeStationId, stations) {
                    DistrictOverview.station(selectedDistrict, stations, homeStationId)
                }
                val otherStations = remember(selectedDistrict, homeStationId, stations) {
                    DistrictOverview.otherStations(selectedDistrict, stations, homeStationId)
                }
                StationsScreen(
                    currentStation = districtStation,
                    currentDistrict = SriLankaDistricts.resolveHome(selectedDistrict).name,
                    otherStations = otherStations,
                    onStationClick = { station ->
                        viewModel.selectStation(station)
                        navController.navigate(Screen.StationDetails.route)
                    },
                    onMapClick = { navController.navigate(Screen.Map.route) }
                )
            }
            composable(Screen.StationDetails.route) {
                StationDetailsScreen(
                    station = selectedStation,
                    readings = readings.filter { it.stationId.isBlank() || it.stationId == selectedStation?.id },
                    onBack = { navController.popBackStack() },
                    onPrediction = { navController.navigate(Screen.Prediction.route) },
                    onAlerts = { navController.navigate(Screen.Alerts.route) },
                    onPreparedness = { navController.navigate(Screen.Preparedness.route) }
                )
            }
            composable(Screen.Prediction.route) {
                val predictionStation = selectedStation ?: homeStation
                LaunchedEffect(predictionStation?.id) {
                    if (selectedStation == null) predictionStation?.let(viewModel::selectStation)
                }
                PredictionScreen(
                    station = predictionStation,
                    readings = readings.filter { it.stationId.isBlank() || it.stationId == predictionStation?.id },
                    onBack = if (navController.previousBackStackEntry?.destination?.route == Screen.StationDetails.route) {
                        { navController.popBackStack() }
                    } else {
                        null
                    },
                    onHistory = { navController.navigate(Screen.History.route) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    station = selectedStation,
                    readings = readings.filter { it.stationId.isBlank() || it.stationId == selectedStation?.id },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Map.route) {
                RiskMapScreen(
                    stations = stations,
                    onBack = { navController.popBackStack() },
                    onStationClick = { station ->
                        viewModel.selectStation(station)
                        navController.navigate(Screen.StationDetails.route)
                    }
                )
            }
            composable(Screen.Alerts.route) {
                AlertsScreen(alerts = alerts) { alert ->
                    viewModel.selectAlert(alert)
                    stations.firstOrNull { it.name == alert.stationName }?.let(viewModel::selectStation)
                    navController.navigate(Screen.AlertDetails.route)
                }
            }
            composable(Screen.AlertDetails.route) {
                AlertDetailsScreen(
                    alert = selectedAlert,
                    onBack = { navController.popBackStack() },
                    onOpenStation = { navController.navigate(Screen.StationDetails.route) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    profile = profile,
                    onSettings = { navController.navigate(Screen.Settings.route) },
                    onPersonalInformation = { navController.navigate(Screen.PersonalInformation.route) },
                    onNotificationPreferences = { navController.navigate(Screen.NotificationPreferences.route) },
                    onPreparedness = { navController.navigate(Screen.Preparedness.route) },
                    onLogout = ::logout,
                    onGenericSettings = { title ->
                        viewModel.selectSettingsPage(title)
                        navController.navigate(Screen.SettingsDetail.route)
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    themeMode = themeMode,
                    onBack = { navController.popBackStack() },
                    onPersonalInformation = { navController.navigate(Screen.PersonalInformation.route) },
                    onNotificationPreferences = { navController.navigate(Screen.NotificationPreferences.route) },
                    onEmergencyContacts = { navController.navigate(Screen.EmergencyContacts.route) },
                    onPreparedness = { navController.navigate(Screen.Preparedness.route) },
                    onThemeChange = viewModel::setThemeMode,
                    onLogout = ::logout,
                    onGenericSettings = { title ->
                        viewModel.selectSettingsPage(title)
                        navController.navigate(Screen.SettingsDetail.route)
                    }
                )
            }
            composable(Screen.Preparedness.route) {
                PreparednessScreen(
                    items = preparednessItems,
                    districtName = SriLankaDistricts.resolveHome(selectedDistrict).name,
                    stations = homeStations,
                    selectedStationId = homeStationId,
                    onBack = { navController.popBackStack() },
                    onToggleItem = viewModel::setPreparednessCompleted,
                    onSelectStation = { station -> viewModel.selectHomeStation(station) },
                    onSelectNearestStation = viewModel::selectNearestStation,
                    onEmergencyContacts = { navController.navigate(Screen.EmergencyContacts.route) }
                )
            }
            composable(Screen.PersonalInformation.route) {
                PersonalInformationScreen(
                    profile = profile,
                    onBack = { navController.popBackStack() },
                    onSave = viewModel::updateProfile
                )
            }
            composable(Screen.NotificationPreferences.route) {
                NotificationPreferencesScreen(
                    settings = notificationSettings,
                    onBack = { navController.popBackStack() },
                    onUpdate = viewModel::updateNotificationSettings
                )
            }
            composable(Screen.EmergencyContacts.route) {
                EmergencyContactsScreen(
                    contacts = emergencyContacts,
                    onBack = { navController.popBackStack() },
                    onAdd = viewModel::addEmergencyContact,
                    onRemove = viewModel::removeEmergencyContact
                )
            }
            composable(Screen.SettingsDetail.route) {
                GenericSettingsDetailScreen(title = settingsTitle, onBack = { navController.popBackStack() })
            }

            composable(Screen.AdminHome.route) {
                val districtStations = remember(selectedDistrict, stations) {
                    DistrictOverview.stationsFor(selectedDistrict, stations)
                }
                AdminDashboardScreen(
                    selectedDistrict = SriLankaDistricts.resolveHome(selectedDistrict).name,
                    districts = SriLankaDistricts.home,
                    districtStations = districtStations,
                    station = homeStation,
                    onSelectDistrict = viewModel::selectDistrict,
                    onSelectStation = { station -> viewModel.selectHomeStation(station) },
                    onIssueAlert = { navController.navigate(Screen.AdminCreateAlert.route) },
                    onStationClick = { station ->
                        viewModel.selectStation(station)
                        navController.navigate(Screen.AdminStationDetails.route)
                    }
                )
            }
            composable(Screen.AdminStations.route) {
                AdminStationsScreen(
                    stations = stations,
                    onStationClick = { station ->
                        viewModel.selectStation(station)
                        navController.navigate(Screen.AdminStationDetails.route)
                    },
                    onAddStation = { navController.navigate(Screen.AdminAddStation.route) }
                )
            }
            composable(Screen.AdminAddStation.route) {
                AdminAddStationScreen(
                    stations = stations,
                    onBack = { navController.popBackStack() },
                    onSave = { station ->
                        viewModel.addStation(station)
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.AdminStationDetails.route) {
                AdminStationDetailsScreen(
                    station = selectedStation ?: homeStation,
                    onBack = { navController.popBackStack() },
                    onViewHistory = { navController.navigate(Screen.AdminHistory.route) },
                    onSendAlert = { navController.navigate(Screen.AdminCreateAlert.route) }
                )
            }
            composable(Screen.AdminAlerts.route) {
                AdminAlertsScreen(
                    alerts = adminAlerts,
                    onCreate = { navController.navigate(Screen.AdminCreateAlert.route) },
                    onAlertClick = { alert ->
                        viewModel.selectAdminAlert(alert)
                        navController.navigate(Screen.AdminAlertDetails.route)
                    }
                )
            }
            composable(Screen.AdminAlertDetails.route) {
                AdminAlertDetailsScreen(
                    alert = selectedAdminAlert,
                    onBack = { navController.popBackStack() },
                    onOpenStation = {
                        stations.firstOrNull { it.name == selectedAdminAlert?.stationName }?.let(viewModel::selectStation)
                        navController.navigate(Screen.AdminStationDetails.route)
                    }
                )
            }
            composable(Screen.AdminCreateAlert.route) {
                AdminCreateAlertScreen(
                    stations = homeStations,
                    selectedStationId = homeStation?.id,
                    onBack = { navController.popBackStack() },
                    onSend = { title, stationName, message, severity, activateSiren ->
                        viewModel.issueAlert(title, stationName, message, severity, activateSiren)
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.AdminUsers.route) {
                AdminUsersScreen(
                    users = managedUsers,
                    onAdd = { navController.navigate(Screen.AdminAddUser.route) }
                )
            }
            composable(Screen.AdminAddUser.route) {
                AdminAddUserScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { user ->
                        viewModel.addManagedUser(user)
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.AdminReports.route) {
                AdminReportsScreen(
                    readings = readings,
                    stations = stations,
                    adminAlerts = adminAlerts,
                    onOpenHistory = { station ->
                        viewModel.selectStation(station)
                        navController.navigate(Screen.AdminHistory.route)
                    }
                )
            }
            composable(Screen.AdminMore.route) {
                AdminMoreScreen(
                    onHistory = { navController.navigate(Screen.AdminHistory.route) },
                    onPrediction = { navController.navigate(Screen.AdminPrediction.route) },
                    onEmergency = { navController.navigate(Screen.AdminEmergency.route) },
                    onReports = { navController.navigate(Screen.AdminReports.route) },
                    onSettings = { navController.navigate(Screen.AdminSettings.route) },
                    onProfile = { navController.navigate(Screen.AdminProfile.route) },
                    onOnboarding = { navController.navigate(Screen.Onboarding.route) }
                )
            }
            composable(Screen.AdminHistory.route) {
                val historyStation = selectedStation ?: homeStation
                AdminHistoryScreen(
                    station = historyStation,
                    stations = stations,
                    readings = readings.filter { it.stationId.isBlank() || it.stationId == historyStation?.id },
                    onBack = { navController.popBackStack() },
                    onSelectStation = { station -> viewModel.selectStation(station) }
                )
            }
            composable(Screen.AdminPrediction.route) {
                AdminPredictionAnalyticsScreen(
                    station = selectedStation ?: homeStation,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminEmergency.route) {
                AdminEmergencyNotificationsScreen(
                    settings = notificationSettings,
                    onBack = { navController.popBackStack() },
                    onUpdate = viewModel::updateNotificationSettings
                )
            }
            composable(Screen.AdminProfile.route) {
                PersonalInformationScreen(
                    profile = profile,
                    onBack = { navController.popBackStack() },
                    onSave = viewModel::updateProfile,
                    darkHeader = true
                )
            }
            composable(Screen.AdminSettings.route) {
                AdminSettingsScreen(
                    profile = profile,
                    onBack = { navController.popBackStack() },
                    onProfile = { navController.navigate(Screen.AdminProfile.route) },
                    onEmergency = { navController.navigate(Screen.AdminEmergency.route) },
                    onPrediction = { navController.navigate(Screen.AdminPrediction.route) },
                    onLogout = ::logout
                )
            }
        }
    }
}
