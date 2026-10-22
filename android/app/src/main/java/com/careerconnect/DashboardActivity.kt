package com.careerconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_layout)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<ImageButton>(R.id.btnNotification)?.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        setupBottomNav()
        setupActions()
        updateDashboardData()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.selectedItemId = R.id.nav_search
        updateDashboardData()
    }

    private fun setupBottomNav() {
        NavigationHelper.setupBottomNavigation(this, R.id.nav_search)
    }

    private fun setupActions() {
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

        findViewById<Button>(R.id.btnMyProfile)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<View>(R.id.cardMetricTotal)?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.cardMetricSaved)?.setOnClickListener {
            startActivity(Intent(this, ApplicationsActivity::class.java))
        }

        findViewById<View>(R.id.cardMetricApplied)?.setOnClickListener {
            startActivity(Intent(this, ApplicationsActivity::class.java))
        }

        findViewById<View>(R.id.cardMetricInterview)?.setOnClickListener {
            startActivity(Intent(this, ApplicationsActivity::class.java))
        }
    }

    private fun updateDashboardData() {
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

        // Live statistics derived directly from JobRepository
        val totalJobs = JobRepository.getAllJobs().size
        val savedCount = JobRepository.getTrackedJobs("Saved").size
        val appliedCount = JobRepository.getTrackedJobs("Applied").size
        val interviewCount = JobRepository.getTrackedJobs("Interview").size

        findViewById<TextView>(R.id.txtMetricTotal)?.text = totalJobs.toString()
        findViewById<TextView>(R.id.txtMetricSaved)?.text = savedCount.toString()
        findViewById<TextView>(R.id.txtMetricApplied)?.text = appliedCount.toString()
        findViewById<TextView>(R.id.txtMetricInterview)?.text = interviewCount.toString()
    }
}
