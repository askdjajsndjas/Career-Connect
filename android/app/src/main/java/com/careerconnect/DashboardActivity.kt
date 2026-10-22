package com.careerconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_layout)

        updateWelcomeSubtitle()

        findViewById<Button>(R.id.btnBrowseInternships)?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("EXTRA_FILTER_TYPE", "Internship")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnTrackApplications)?.setOnClickListener {
            startActivity(Intent(this, ApplicationsActivity::class.java))
        }

        findViewById<Button>(R.id.btnCVBuilder)?.setOnClickListener {
            startActivity(Intent(this, CVBuilderActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateWelcomeSubtitle()
    }

    private fun updateWelcomeSubtitle() {
        val user = FirebaseAuth.getInstance().currentUser
        val prefs = getSharedPreferences(ProfileActivity.profilePrefsName(user?.uid), Context.MODE_PRIVATE)
        val savedName = ProfileActivity.readSecurePref(prefs, "fullName", null)
        val firstName = ProfileActivity.resolveFirstName(
            savedName = savedName,
            firebaseDisplayName = user?.displayName,
            firebaseEmail = user?.email,
            defaultFirstName = "Amina"
        )
        findViewById<TextView>(R.id.txtSubtitle)?.text = "Welcome to Career Connect, $firstName"
    }
}
