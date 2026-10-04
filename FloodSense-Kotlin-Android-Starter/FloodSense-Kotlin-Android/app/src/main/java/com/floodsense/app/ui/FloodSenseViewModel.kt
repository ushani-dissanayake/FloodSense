package com.floodsense.app.ui

import androidx.lifecycle.ViewModel
import com.floodsense.app.alerts.CitizenPushRegistrar
import com.floodsense.app.alerts.EmergencySmsDispatcher
import com.floodsense.app.data.AdminAlert
import com.floodsense.app.data.AlertDelivery
import com.floodsense.app.data.AlertSeverity
import com.floodsense.app.data.AppRole
import com.floodsense.app.data.AuthRepository
import com.floodsense.app.data.DistrictOverview
import com.floodsense.app.data.EmergencyContact
import com.floodsense.app.data.FirebaseFloodRepository
import com.floodsense.app.data.FloodAlert
import com.floodsense.app.data.FloodRepository
import com.floodsense.app.data.FloodRiskEngine
import com.floodsense.app.data.ManagedUser
import com.floodsense.app.data.NotificationSettings
import com.floodsense.app.data.SriLankaDistricts
import com.floodsense.app.data.Station
import com.floodsense.app.data.ThemeMode
import com.floodsense.app.data.UserProfile
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AuthStatus { Checking, SignedOut, Citizen, Admin }

class FloodSenseViewModel(
    private val repository: FloodRepository = FirebaseFloodRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    val stations = repository.stations
    val readings = repository.readings
    val alerts = repository.alerts
    val preparednessItems = repository.preparednessItems
    val managedUsers = repository.managedUsers
    val adminAlerts = repository.adminAlerts

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _role = MutableStateFlow(AppRole.USER)
    val role: StateFlow<AppRole> = _role.asStateFlow()

    private val _authStatus = MutableStateFlow(AuthStatus.Checking)
    val authStatus: StateFlow<AuthStatus> = _authStatus.asStateFlow()

    private val _authBusy = MutableStateFlow(false)
    val authBusy: StateFlow<Boolean> = _authBusy.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _notificationSettings = MutableStateFlow(NotificationSettings())
    val notificationSettings: StateFlow<NotificationSettings> = _notificationSettings.asStateFlow()

    private val _emergencyContacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val emergencyContacts: StateFlow<List<EmergencyContact>> = _emergencyContacts.asStateFlow()

    private val _selectedDistrict = MutableStateFlow(SriLankaDistricts.DEFAULT)
    val selectedDistrict: StateFlow<String> = _selectedDistrict.asStateFlow()

    private val _homeStationId = MutableStateFlow(SriLankaDistricts.DEFAULT_STATION_ID)
    val homeStationId: StateFlow<String> = _homeStationId.asStateFlow()

    private val _selectedStation = MutableStateFlow<Station?>(null)
    val selectedStation: StateFlow<Station?> = _selectedStation.asStateFlow()

    private val _selectedAlert = MutableStateFlow<FloodAlert?>(null)
    val selectedAlert: StateFlow<FloodAlert?> = _selectedAlert.asStateFlow()

    private val _selectedAdminAlert = MutableStateFlow<AdminAlert?>(null)
    val selectedAdminAlert: StateFlow<AdminAlert?> = _selectedAdminAlert.asStateFlow()

    private val _selectedSettingsTitle = MutableStateFlow("Account Settings")
    val selectedSettingsTitle: StateFlow<String> = _selectedSettingsTitle.asStateFlow()

    init {
        authRepository.restoreSession { profile, role ->
            if (profile != null && role != null) {
                completeSession(profile, role)
            } else {
                _authStatus.value = AuthStatus.SignedOut
            }
        }
    }

    fun clearAuthFeedback() {
        _authError.value = null
        _authMessage.value = null
    }

    fun setRole(role: AppRole) {
        _role.value = role
    }

    fun registerCitizen(name: String, email: String, password: String, onSuccess: () -> Unit) {
        runAuth {
            authRepository.registerCitizen(name, email, password, onSuccess = {
                completeSession(it, AppRole.USER)
                onSuccess()
            }, onError = { fail(it) })
        }
    }

    fun signInCitizen(email: String, password: String, onSuccess: () -> Unit) {
        runAuth {
            authRepository.signInCitizen(email, password, onSuccess = {
                completeSession(it, AppRole.USER)
                onSuccess()
            }, onError = { fail(it) })
        }
    }

    fun signInAdmin(
        email: String,
        password: String,
        createIfMissing: Boolean,
        name: String,
        onSuccess: () -> Unit
    ) {
        runAuth {
            authRepository.signInAdmin(
                email = email,
                password = password,
                createIfMissing = createIfMissing,
                name = name,
                onSuccess = {
                    completeSession(it, AppRole.ADMIN)
                    onSuccess()
                },
                onError = { fail(it) }
            )
        }
    }

    fun sendPasswordReset(email: String) {
        runAuth {
            authRepository.sendPasswordReset(email, onSuccess = {
                _authBusy.value = false
                _authMessage.value = "Password reset email sent. Check your inbox."
            }, onError = { fail(it) })
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authError.value = null
        _authMessage.value = null
        _profile.value = UserProfile()
        _selectedDistrict.value = SriLankaDistricts.DEFAULT
        _homeStationId.value = SriLankaDistricts.DEFAULT_STATION_ID
        _role.value = AppRole.USER
        _emergencyContacts.value = emptyList()
        _authStatus.value = AuthStatus.SignedOut
    }

    private fun runAuth(block: () -> Unit) {
        _authBusy.value = true
        _authError.value = null
        _authMessage.value = null
        block()
    }

    private fun fail(message: String) {
        _authBusy.value = false
        _authError.value = message
    }

    private fun completeSession(profile: UserProfile, role: AppRole) {
        _profile.value = profile
        applyDistrict(profile.district, persist = false)
        _role.value = role
        _authBusy.value = false
        _authError.value = null
        _authStatus.value = if (role == AppRole.ADMIN) AuthStatus.Admin else AuthStatus.Citizen
        FirebaseMessaging.getInstance().token.addOnSuccessListener(CitizenPushRegistrar::saveToken)
        authRepository.currentUid?.let { uid ->
            authRepository.loadEmergencyContacts(uid) { contacts ->
                _emergencyContacts.value = contacts
            }
        }
    }

    fun selectDistrict(name: String) {
        applyDistrict(name, persist = true)
    }

    fun selectHomeStation(stationId: String) {
        val station = DistrictOverview.allHomeStations(stations.value)
            .firstOrNull { it.id == stationId } ?: return
        selectHomeStation(station)
    }

    fun selectHomeStation(station: Station) {
        val district = SriLankaDistricts.resolveHome(station.district)
        _selectedDistrict.value = district.name
        _homeStationId.value = station.id
        _selectedStation.value = station
        if (_profile.value.district != district.name) {
            val updated = _profile.value.copy(district = district.name)
            _profile.value = updated
            authRepository.currentUid?.let { uid ->
                authRepository.saveProfile(uid, updated, _role.value)
            }
        }
        repository.setPreparednessCompleted("station", true)
    }

    fun selectNearestStation(latitude: Double, longitude: Double): Station? {
        val nearest = DistrictOverview.nearestStation(latitude, longitude, stations.value) ?: return null
        selectHomeStation(nearest)
        return nearest
    }

    fun selectStation(station: Station) {
        _selectedStation.value = station
        val ids = DistrictOverview.correspondingIds(_selectedDistrict.value, stations.value)
        if (station.id in ids) {
            _homeStationId.value = station.id
        }
    }

    fun selectAlert(alert: FloodAlert) {
        _selectedAlert.value = alert
    }

    fun selectAdminAlert(alert: AdminAlert) {
        _selectedAdminAlert.value = alert
    }

    fun selectSettingsPage(title: String) {
        _selectedSettingsTitle.value = title
    }

    fun updateProfile(profile: UserProfile) {
        _profile.value = profile
        applyDistrict(profile.district, persist = false)
        authRepository.currentUid?.let { uid ->
            authRepository.saveProfile(uid, profile, _role.value)
        }
    }

    private fun applyDistrict(name: String, persist: Boolean) {
        val resolved = SriLankaDistricts.resolveHome(name)
        _selectedDistrict.value = resolved.name
        _homeStationId.value = resolved.stationId
        if (persist && _profile.value.district != resolved.name) {
            val updated = _profile.value.copy(district = resolved.name)
            _profile.value = updated
            authRepository.currentUid?.let { uid ->
                authRepository.saveProfile(uid, updated, _role.value)
            }
        }
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        _notificationSettings.value = settings
    }

    fun addEmergencyContact(contact: EmergencyContact) {
        persistContacts(_emergencyContacts.value + contact)
    }

    fun removeEmergencyContact(contact: EmergencyContact) {
        persistContacts(_emergencyContacts.value - contact)
    }

    private fun persistContacts(contacts: List<EmergencyContact>) {
        _emergencyContacts.value = contacts
        authRepository.currentUid?.let { authRepository.saveEmergencyContacts(it, contacts) }
    }

    fun setPreparednessCompleted(itemId: String, completed: Boolean) {
        repository.setPreparednessCompleted(itemId, completed)
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun addStation(station: Station) {
        val scored = FloodRiskEngine.apply(station)
        repository.addStation(scored)
        _selectedStation.value = scored
    }

    fun updateStation(station: Station) {
        val scored = FloodRiskEngine.apply(station)
        repository.updateStation(scored)
        _selectedStation.value = scored
    }

    fun addManagedUser(user: ManagedUser) {
        repository.addManagedUser(user)
    }

    fun issueAlert(
        title: String,
        stationName: String,
        message: String,
        severity: AlertSeverity,
        activateSiren: Boolean = false
    ) {
        val alert = AdminAlert(
                id = "aa-${System.currentTimeMillis()}",
                title = title,
                stationName = stationName,
                message = message,
                time = "Just now",
                recipients = 426,
                severity = severity,
                delivery = AlertDelivery.DELIVERED
            )
        repository.addAdminAlert(alert)
        if (severity == AlertSeverity.CRITICAL) {
            EmergencySmsDispatcher.notifyContacts(title, message, alertId = alert.id)
        }
        if (activateSiren) {
            matchStation(stationName)?.let { setSiren(it.id, true) }
        }
    }

    fun setSiren(stationId: String, on: Boolean) {
        repository.setSirenCommand(stationId, on)
        val updated = stations.value.firstOrNull { it.id == stationId }
        if (updated != null && _selectedStation.value?.id == stationId) {
            _selectedStation.value = updated.copy(sirenCommand = on, sirenMute = !on)
        }
    }

    private fun matchStation(nameOrId: String): Station? {
        val key = nameOrId.trim()
        return stations.value.firstOrNull { it.id.equals(key, ignoreCase = true) }
            ?: stations.value.firstOrNull { it.name.equals(key, ignoreCase = true) }
            ?: stations.value.firstOrNull { it.name.contains(key, ignoreCase = true) }
            ?: _selectedStation.value
    }
}
