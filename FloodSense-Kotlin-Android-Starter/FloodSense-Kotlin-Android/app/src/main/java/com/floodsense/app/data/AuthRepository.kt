package com.floodsense.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    val currentUid: String? get() = auth.currentUser?.uid

    fun restoreSession(onResult: (UserProfile?, AppRole?) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(null, null)
            return
        }
        loadProfile(user.uid) { profile, role ->
            onResult(
                profile ?: UserProfile(
                    fullName = user.displayName ?: "FloodSense User",
                    email = user.email.orEmpty(),
                    phone = user.phoneNumber.orEmpty()
                ),
                role
            )
        }
    }

    fun registerCitizen(
        name: String,
        email: String,
        password: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener onError("Account was not created.")
                user.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
                )
                val profile = UserProfile(fullName = name.trim(), email = email.trim())
                saveProfile(user.uid, profile, AppRole.USER) {
                    onSuccess(profile)
                }
            }
            .addOnFailureListener { onError(it.toAuthMessage()) }
    }

    fun signInCitizen(
        email: String,
        password: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                val user = result.user ?: return@addOnSuccessListener onError("Sign-in failed.")
                loadProfile(user.uid) { stored, role ->
                    if (role == AppRole.ADMIN) {
                        onError("This account is an administrator. Use Administrator login.")
                        return@loadProfile
                    }
                    val profile = stored ?: UserProfile(
                        fullName = user.displayName ?: "FloodSense User",
                        email = user.email ?: email.trim()
                    )
                    if (stored == null) {
                        saveProfile(user.uid, profile, AppRole.USER)
                    }
                    onSuccess(profile)
                }
            }
            .addOnFailureListener { onError(it.toAuthMessage()) }
    }

    fun signInAdmin(
        email: String,
        password: String,
        createIfMissing: Boolean,
        name: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val task = if (createIfMissing) {
            auth.createUserWithEmailAndPassword(email.trim(), password)
        } else {
            auth.signInWithEmailAndPassword(email.trim(), password)
        }
        task.addOnSuccessListener { result ->
            val user = result.user ?: return@addOnSuccessListener onError("Authentication failed.")
            if (createIfMissing) {
                user.updateProfile(
                    UserProfileChangeRequest.Builder().setDisplayName(name.trim().ifBlank { "Administrator" }).build()
                )
                val profile = UserProfile(
                    fullName = name.trim().ifBlank { "Administrator" },
                    email = email.trim()
                )
                saveProfile(user.uid, profile, AppRole.ADMIN) {
                    onSuccess(profile)
                }
            } else {
                loadProfile(user.uid) { stored, role ->
                    if (role != null && role != AppRole.ADMIN) {
                        onError("This account is not an administrator.")
                        return@loadProfile
                    }
                    val profile = stored ?: UserProfile(
                        fullName = user.displayName ?: "Administrator",
                        email = user.email ?: email.trim()
                    )
                    if (stored == null) {
                        saveProfile(user.uid, profile, AppRole.ADMIN) {
                            onSuccess(profile)
                        }
                    } else {
                        onSuccess(profile)
                    }
                }
            }
        }.addOnFailureListener { error ->
            val alreadyExists = error.message?.contains("email address is already", ignoreCase = true) == true
            if (createIfMissing && alreadyExists) {
                signInAdmin(email, password, createIfMissing = false, name = name, onSuccess = onSuccess, onError = onError)
            } else {
                onError(error.toAuthMessage())
            }
        }
    }

    fun sendPasswordReset(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.toAuthMessage()) }
    }

    fun signOut() {
        auth.signOut()
    }

    fun saveProfile(uid: String, profile: UserProfile, role: AppRole, onDone: () -> Unit = {}) {
        database.getReference("users/$uid").updateChildren(
            mapOf(
                "fullName" to profile.fullName,
                "email" to profile.email,
                "phone" to profile.phone,
                "district" to profile.district,
                "role" to role.name
            )
        ).addOnCompleteListener { onDone() }
    }

    fun saveEmergencyContacts(uid: String, contacts: List<EmergencyContact>) {
        val payload = contacts.associate { contact ->
            val key = AuthRepository.normalizePhone(contact.phone)?.filter(Char::isDigit)
                ?: contact.phone.filter(Char::isDigit).ifBlank { contact.name.hashCode().toString() }
            key to mapOf(
                "name" to contact.name,
                "phone" to (AuthRepository.normalizePhone(contact.phone) ?: contact.phone.trim()),
                "relationship" to contact.relationship
            )
        }
        database.getReference("users/$uid/emergency_contacts").setValue(payload)
        // Readable by admin and by any signed-in device that issues a Critical alert.
        database.getReference("FloodSystem/sms_recipients/$uid").setValue(payload)
    }

    fun loadEmergencyContacts(uid: String, onLoaded: (List<EmergencyContact>) -> Unit) {
        database.getReference("users/$uid/emergency_contacts").get()
            .addOnSuccessListener { snap ->
                val contacts = snap.children.mapNotNull { child ->
                    val name = child.child("name").getValue(String::class.java) ?: return@mapNotNull null
                    val phone = child.child("phone").getValue(String::class.java) ?: return@mapNotNull null
                    EmergencyContact(
                        name = name,
                        phone = phone,
                        relationship = child.child("relationship").getValue(String::class.java) ?: "Contact"
                    )
                }
                onLoaded(contacts)
            }
            .addOnFailureListener { onLoaded(emptyList()) }
    }

    private fun loadProfile(uid: String, onLoaded: (UserProfile?, AppRole?) -> Unit) {
        database.getReference("users/$uid").get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    onLoaded(null, null)
                    return@addOnSuccessListener
                }
                val role = when (snap.child("role").getValue(String::class.java)) {
                    AppRole.ADMIN.name -> AppRole.ADMIN
                    else -> AppRole.USER
                }
                onLoaded(
                    UserProfile(
                        fullName = snap.child("fullName").getValue(String::class.java) ?: "FloodSense User",
                        email = snap.child("email").getValue(String::class.java).orEmpty(),
                        phone = snap.child("phone").getValue(String::class.java).orEmpty(),
                        district = snap.child("district").getValue(String::class.java) ?: "Kalutara District"
                    ),
                    role
                )
            }
            .addOnFailureListener { onLoaded(null, null) }
    }

    companion object {
        fun normalizePhone(raw: String): String? {
            val digits = raw.filter(Char::isDigit)
            val e164 = when {
                raw.trim().startsWith("+") && digits.length in 10..15 -> "+$digits"
                digits.startsWith("94") && digits.length == 11 -> "+$digits"
                digits.startsWith("0") && digits.length == 10 -> "+94${digits.drop(1)}"
                digits.length == 9 -> "+94$digits"
                else -> null
            }
            return e164
        }
    }
}

private fun Throwable.toAuthMessage(): String {
    val text = message.orEmpty()
    return when {
        text.contains("email address is already", ignoreCase = true) -> "An account already exists for that email."
        text.contains("password is invalid", ignoreCase = true) ||
            text.contains("INVALID_LOGIN", ignoreCase = true) -> "Incorrect email or password."
        text.contains("no user record", ignoreCase = true) ||
            text.contains("USER_NOT_FOUND", ignoreCase = true) -> "No account found for that email."
        text.contains("badly formatted", ignoreCase = true) -> "Enter a valid email address."
        text.contains("too-many-requests", ignoreCase = true) -> "Too many attempts. Try again later."
        text.contains("network", ignoreCase = true) -> "Check your internet connection and try again."
        text.isBlank() -> "Authentication failed."
        else -> text
    }
}
