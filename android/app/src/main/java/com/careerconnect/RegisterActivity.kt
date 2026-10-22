package com.careerconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val txtFullName = findViewById<EditText>(R.id.txtFullName)
        val txtEmail = findViewById<EditText>(R.id.txtEmail)
        val txtPassword = findViewById<EditText>(R.id.txtPassword)
        val txtConfirm = findViewById<EditText>(R.id.txtConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnRegister.setOnClickListener {
            val name = txtFullName.text.toString().trim()
            val email = txtEmail.text.toString().trim()
            val password = txtPassword.text.toString()
            val confirm = txtConfirm.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (!task.isSuccessful) {
                        btnRegister.isEnabled = true
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnCompleteListener
                    }

                    val user = task.result?.user
                    if (user == null) {
                        btnRegister.isEnabled = true
                        return@addOnCompleteListener
                    }

                    user.updateProfile(
                        UserProfileChangeRequest.Builder().setDisplayName(name).build()
                    )

                    val profile = hashMapOf(
                        "fullName" to name,
                        "email" to email,
                        "role" to "student",
                        "createdAt" to Timestamp.now()
                    )
                    db.collection("users").document(user.uid).set(profile)
                        .addOnCompleteListener {
                            if (!it.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Account created, but saving your profile failed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                }
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}