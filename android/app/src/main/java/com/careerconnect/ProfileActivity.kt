package com.careerconnect

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { finish() }

        val txtProfileName = findViewById<TextView>(R.id.txtProfileName)
        val txtProfileEmail = findViewById<TextView>(R.id.txtProfileEmail)
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEducation = findViewById<EditText>(R.id.etEducation)
        val etQualification = findViewById<EditText>(R.id.etQualification)
        val etSkills = findViewById<EditText>(R.id.etSkills)

        val prefs = getSharedPreferences(profilePrefsName(user?.uid), Context.MODE_PRIVATE)

        val savedFullName = readSecurePref(prefs, "fullName", null)
        val displayName = resolveFullName(
            savedName = savedFullName,
            firebaseDisplayName = user?.displayName,
            firebaseEmail = user?.email
        )
        val email = user?.email ?: "student@university.ac.za"

        txtProfileName?.text = displayName
        txtProfileEmail?.text = email
        etFullName?.setText(displayName)
        etPhone?.setText(readSecurePref(prefs, "phone", "+27 82 123 4567"))
        etEducation?.setText(readSecurePref(prefs, "education", "University of Johannesburg"))
        etQualification?.setText(readSecurePref(prefs, "qualification", "BSc Information Technology"))
        etSkills?.setText(readSecurePref(prefs, "skills", "Kotlin, Java, Python, Android Studio, Git"))

        findViewById<Button>(R.id.btnSaveProfile)?.setOnClickListener {
            val updatedName = etFullName?.text?.toString()?.trim() ?: ""
            val updatedPhone = etPhone?.text?.toString()?.trim() ?: ""
            val updatedEdu = etEducation?.text?.toString()?.trim() ?: ""
            val updatedQual = etQualification?.text?.toString()?.trim() ?: ""
            val updatedSkills = etSkills?.text?.toString()?.trim() ?: ""

            if (updatedName.isEmpty()) {
                etFullName?.error = "Please enter your full name"
                Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            writeSecurePref(prefs, "fullName", updatedName)
            writeSecurePref(prefs, "phone", updatedPhone)
            writeSecurePref(prefs, "education", updatedEdu)
            writeSecurePref(prefs, "qualification", updatedQual)
            writeSecurePref(prefs, "skills", updatedSkills)

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
            txtProfileName?.text = resolved
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnLogout)?.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out of Career Connect?")
            .setPositiveButton("Log Out") { _, _ ->
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
                // Transparently migrate existing plaintext value on next read if KeyStore is available
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
