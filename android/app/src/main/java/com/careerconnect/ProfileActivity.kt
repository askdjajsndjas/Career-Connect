package com.careerconnect

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { finish() }

        setupBottomNav()
        loadProfileData()
        setupActions()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.selectedItemId = R.id.nav_profile
        if (!isEditMode) {
            loadProfileData()
        }
    }

    private fun setupBottomNav() {
        NavigationHelper.setupBottomNavigation(this, R.id.nav_profile)
    }

    private fun loadProfileData() {
        val user = auth.currentUser
        val prefs = getSharedPreferences(profilePrefsName(user?.uid), Context.MODE_PRIVATE)

        val savedFullName = readSecurePref(prefs, "fullName", null)
        val displayName = resolveFullName(
            savedName = savedFullName,
            firebaseDisplayName = user?.displayName,
            firebaseEmail = user?.email
        )
        val email = user?.email ?: "student@university.ac.za"

        findViewById<TextView>(R.id.txtProfileName)?.text = displayName
        findViewById<TextView>(R.id.txtProfileEmail)?.text = email

        findViewById<EditText>(R.id.etFullName)?.setText(displayName)
        findViewById<EditText>(R.id.etPhone)?.setText(readSecurePref(prefs, "phone", "+27 82 123 4567"))
        findViewById<EditText>(R.id.etEducation)?.setText(readSecurePref(prefs, "education", "University of Johannesburg"))
        findViewById<EditText>(R.id.etQualification)?.setText(readSecurePref(prefs, "qualification", "BSc Information Technology"))
        findViewById<EditText>(R.id.etSkills)?.setText(readSecurePref(prefs, "skills", "Kotlin, Java, Python, Android Studio, Git"))
    }

    private fun setupActions() {
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        val layoutEditActions = findViewById<LinearLayout>(R.id.layoutEditActions)
        val btnCancelEdit = findViewById<Button>(R.id.btnCancelEdit)
        val btnSaveProfile = findViewById<Button>(R.id.btnSaveProfile)
        val btnGoToCVBuilder = findViewById<Button>(R.id.btnGoToCVBuilder)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnEditProfile?.setOnClickListener {
            setEditMode(true)
        }

        btnCancelEdit?.setOnClickListener {
            loadProfileData()
            clearFieldErrors()
            setEditMode(false)
            Toast.makeText(this, "Changes cancelled", Toast.LENGTH_SHORT).show()
        }

        btnSaveProfile?.setOnClickListener {
            val user = auth.currentUser
            val prefs = getSharedPreferences(profilePrefsName(user?.uid), Context.MODE_PRIVATE)

            val etFullName = findViewById<EditText>(R.id.etFullName)
            val etPhone = findViewById<EditText>(R.id.etPhone)
            val etEducation = findViewById<EditText>(R.id.etEducation)
            val etQualification = findViewById<EditText>(R.id.etQualification)
            val etSkills = findViewById<EditText>(R.id.etSkills)

            val updatedName = etFullName?.text?.toString()?.trim().orEmpty()
            val updatedPhone = etPhone?.text?.toString()?.trim().orEmpty()
            val updatedEdu = etEducation?.text?.toString()?.trim().orEmpty()
            val updatedQual = etQualification?.text?.toString()?.trim().orEmpty()
            val updatedSkills = etSkills?.text?.toString()?.trim().orEmpty()

            if (updatedName.isEmpty()) {
                etFullName?.error = "Please enter your full name"
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (updatedPhone.isEmpty()) {
                etPhone?.error = "Please enter your contact number"
                Toast.makeText(this, "Please enter your contact number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            writeSecurePref(prefs, "fullName", updatedName)
            writeSecurePref(prefs, "phone", updatedPhone)
            writeSecurePref(prefs, "education", updatedEdu)
            writeSecurePref(prefs, "qualification", updatedQual)
            writeSecurePref(prefs, "skills", updatedSkills)

            // Update Firebase display name asynchronously (without overriding local precedence)
            user?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(updatedName)
                    .build()
            )

            val resolved = resolveFullName(
                savedName = updatedName,
                firebaseDisplayName = user?.displayName,
                firebaseEmail = user?.email
            )
            findViewById<TextView>(R.id.txtProfileName)?.text = resolved

            setEditMode(false)
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }

        btnGoToCVBuilder?.setOnClickListener {
            startActivity(Intent(this, CVBuilderActivity::class.java))
        }

        btnLogout?.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setEditMode(enabled: Boolean) {
        isEditMode = enabled

        val fields = listOf(
            findViewById<EditText>(R.id.etFullName),
            findViewById<EditText>(R.id.etPhone),
            findViewById<EditText>(R.id.etEducation),
            findViewById<EditText>(R.id.etQualification),
            findViewById<EditText>(R.id.etSkills)
        )

        for (field in fields) {
            field?.isEnabled = enabled
            field?.isFocusable = enabled
            field?.isFocusableInTouchMode = enabled
        }

        if (enabled) {
            fields.firstOrNull()?.requestFocus()
        }

        findViewById<View>(R.id.btnEditProfile)?.visibility = if (enabled) View.GONE else View.VISIBLE
        findViewById<View>(R.id.layoutEditActions)?.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun clearFieldErrors() {
        findViewById<EditText>(R.id.etFullName)?.error = null
        findViewById<EditText>(R.id.etPhone)?.error = null
        findViewById<EditText>(R.id.etEducation)?.error = null
        findViewById<EditText>(R.id.etQualification)?.error = null
        findViewById<EditText>(R.id.etSkills)?.error = null
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out of Career Connect?")
            .setPositiveButton("Log Out") { _, _ ->
                StatusNotifier.stop()
                auth.signOut()
                Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, WelcomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "career_connect_prefs_aes_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ENCRYPTED_PREFIX = "enc:v1:"
        private const val GCM_TAG_BITS = 128

        fun profilePrefsName(uid: String?): String = "profile_prefs_${uid ?: "default"}"

        fun resolveFullName(
            savedName: String?,
            firebaseDisplayName: String?,
            firebaseEmail: String? = null,
            defaultName: String = "Amina Dlamini"
        ): String {
            val cleanedSaved = savedName?.trim()
            if (!cleanedSaved.isNullOrEmpty()) return cleanedSaved

            val cleanedFirebase = firebaseDisplayName?.trim()
            if (!cleanedFirebase.isNullOrEmpty()) return cleanedFirebase

            val emailPrefix = firebaseEmail?.substringBefore("@")?.trim()
            if (!emailPrefix.isNullOrEmpty()) return emailPrefix

            return defaultName
        }

        fun resolveFirstName(
            savedName: String?,
            firebaseDisplayName: String?,
            firebaseEmail: String? = null,
            defaultFirstName: String = "Amina"
        ): String {
            val fullName = resolveFullName(
                savedName = savedName,
                firebaseDisplayName = firebaseDisplayName,
                firebaseEmail = firebaseEmail,
                defaultName = defaultFirstName
            )
            return fullName.trim().substringBefore(" ").ifEmpty { defaultFirstName }
        }

        fun writeSecurePref(prefs: SharedPreferences, key: String, plaintext: String) {
            val storedValue = encryptValue(plaintext) ?: plaintext
            prefs.edit().putString(key, storedValue).apply()
        }

        fun readSecurePref(prefs: SharedPreferences, key: String, defaultValue: String?): String? {
            val raw = prefs.getString(key, null) ?: return defaultValue
            if (!raw.startsWith(ENCRYPTED_PREFIX)) {
                encryptValue(raw)?.let { encrypted ->
                    prefs.edit().putString(key, encrypted).apply()
                }
                return raw
            }
            return decryptValue(raw) ?: defaultValue
        }

        private fun getOrCreateSecretKey(): SecretKey? {
            return try {
                val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
                val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                if (existing != null) {
                    existing.secretKey
                } else {
                    val keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        KEYSTORE_PROVIDER
                    )
                    val spec = KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                    keyGenerator.init(spec)
                    keyGenerator.generateKey()
                }
            } catch (_: Exception) {
                null
            }
        }

        private fun encryptValue(plaintext: String): String? {
            return try {
                val secretKey = getOrCreateSecretKey() ?: return null
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
                val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
                val ctB64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
                "$ENCRYPTED_PREFIX$ivB64:$ctB64"
            } catch (_: Exception) {
                null
            }
        }

        private fun decryptValue(encoded: String): String? {
            return try {
                val payload = encoded.removePrefix(ENCRYPTED_PREFIX)
                val parts = payload.split(":", limit = 2)
                if (parts.size != 2) return null
                val iv = Base64.decode(parts[0], Base64.NO_WRAP)
                val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)
                val secretKey = getOrCreateSecretKey() ?: return null
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
                String(cipher.doFinal(ciphertext), Charsets.UTF_8)
            } catch (_: Exception) {
                null
            }
        }
    }
}
