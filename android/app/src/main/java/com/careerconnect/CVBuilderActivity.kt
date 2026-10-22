package com.careerconnect

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class CVBuilderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cv_builder)

        val user = FirebaseAuth.getInstance().currentUser
        val cvPrefs = getSharedPreferences("cv_prefs_${user?.uid ?: "default"}", Context.MODE_PRIVATE)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { finish() }

        findViewById<View>(R.id.btnEditProfileLink)?.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        setupBottomNav()

        val etSummary = findViewById<EditText>(R.id.etSummary)
        val etExperience = findViewById<EditText>(R.id.etExperience)

        val defaultSummary = "Dedicated Computer Science student passionate about mobile application development and software engineering."
        val defaultExperience = "Academic Projects: Career Connect Android App, Database Management System"

        val savedSummary = ProfileActivity.readSecurePref(cvPrefs, "summary", defaultSummary) ?: defaultSummary
        val savedExperience = ProfileActivity.readSecurePref(cvPrefs, "experience", defaultExperience) ?: defaultExperience

        etSummary?.setText(savedSummary)
        etExperience?.setText(savedExperience)

        populateCVPreviewCard(savedSummary, savedExperience)

        // Real-time TextWatchers for instant preview synchronization
        val liveTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                val currentSummary = etSummary?.text?.toString() ?: ""
                val currentExp = etExperience?.text?.toString() ?: ""
                populateCVPreviewCard(currentSummary, currentExp)
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etSummary?.addTextChangedListener(liveTextWatcher)
        etExperience?.addTextChangedListener(liveTextWatcher)

        findViewById<Button>(R.id.btnSaveCV)?.setOnClickListener {
            val summary = etSummary?.text?.toString()?.trim().orEmpty()
            val experience = etExperience?.text?.toString()?.trim().orEmpty()

            if (summary.isEmpty()) {
                etSummary?.error = "Please enter a professional summary"
                Toast.makeText(this, "Please enter a professional summary", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (experience.isEmpty()) {
                etExperience?.error = "Please enter your work or project experience"
                Toast.makeText(this, "Please enter your work or project experience", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ProfileActivity.writeSecurePref(cvPrefs, "summary", summary)
            ProfileActivity.writeSecurePref(cvPrefs, "experience", experience)

            val formattedCv = populateCVPreviewCard(summary, experience)
            Toast.makeText(this, "CV saved and preview updated", Toast.LENGTH_SHORT).show()

            AlertDialog.Builder(this)
                .setTitle("Generated CV Preview")
                .setMessage(formattedCv)
                .setPositiveButton("Share CV") { _, _ ->
                    shareCvText(formattedCv)
                }
                .setNegativeButton("Close", null)
                .show()
        }

        findViewById<Button>(R.id.btnShareCV)?.setOnClickListener {
            val summary = etSummary?.text?.toString()?.trim() ?: savedSummary
            val experience = etExperience?.text?.toString()?.trim() ?: savedExperience
            val formattedCv = populateCVPreviewCard(summary, experience)
            shareCvText(formattedCv)
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.selectedItemId = R.id.nav_cv
        val etSummary = findViewById<EditText>(R.id.etSummary)
        val etExperience = findViewById<EditText>(R.id.etExperience)
        val summary = etSummary?.text?.toString()?.trim().orEmpty()
        val experience = etExperience?.text?.toString()?.trim().orEmpty()
        populateCVPreviewCard(summary, experience)
    }

    private fun setupBottomNav() {
        NavigationHelper.setupBottomNavigation(this, R.id.nav_cv)
    }

    private fun populateCVPreviewCard(summary: String, experience: String): String {
        val user = FirebaseAuth.getInstance().currentUser
        val profilePrefs = getSharedPreferences(ProfileActivity.profilePrefsName(user?.uid), Context.MODE_PRIVATE)

        val fullName = ProfileActivity.resolveFullName(
            savedName = ProfileActivity.readSecurePref(profilePrefs, "fullName", null),
            firebaseDisplayName = user?.displayName,
            firebaseEmail = user?.email
        )
        val email = user?.email ?: "student@university.ac.za"
        val phone = ProfileActivity.readSecurePref(profilePrefs, "phone", "+27 82 123 4567") ?: "+27 82 123 4567"
        val education = ProfileActivity.readSecurePref(profilePrefs, "education", "University of Johannesburg") ?: "University of Johannesburg"
        val qualification = ProfileActivity.readSecurePref(profilePrefs, "qualification", "BSc Information Technology") ?: "BSc Information Technology"
        val skills = ProfileActivity.readSecurePref(profilePrefs, "skills", "Kotlin, Java, Python, Android Studio, Git") ?: "Kotlin, Java, Python, Android Studio, Git"

        findViewById<CardView>(R.id.cardCVPreview)?.visibility = View.VISIBLE
        findViewById<TextView>(R.id.txtCVPreviewName)?.text = fullName
        findViewById<TextView>(R.id.txtCVPreviewContact)?.text = "$email • $phone"
        findViewById<TextView>(R.id.txtCVPreviewEducation)?.text = "$qualification — $education"
        findViewById<TextView>(R.id.txtCVPreviewSummary)?.text = summary.ifEmpty { "No summary entered yet." }
        findViewById<TextView>(R.id.txtCVPreviewSkills)?.text = skills
        findViewById<TextView>(R.id.txtCVPreviewExperience)?.text = experience.ifEmpty { "No work or project experience entered yet." }

        return formatCvPreview(
            fullName = fullName,
            email = email,
            phone = phone,
            education = education,
            qualification = qualification,
            skills = skills,
            summary = summary,
            experience = experience
        )
    }

    private fun shareCvText(formattedCv: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Curriculum Vitae - Career Connect")
            putExtra(Intent.EXTRA_TEXT, formattedCv)
        }
        startActivity(Intent.createChooser(shareIntent, "Share CV via"))
    }

    companion object {
        fun formatCvPreview(
            fullName: String,
            email: String,
            phone: String,
            education: String,
            qualification: String,
            skills: String,
            summary: String,
            experience: String
        ): String {
            return buildString {
                appendLine(fullName)
                appendLine("$email | $phone")
                appendLine("$qualification — $education")
                appendLine()
                appendLine("PROFESSIONAL SUMMARY")
                appendLine(summary)
                appendLine()
                appendLine("KEY SKILLS")
                appendLine(skills)
                appendLine()
                appendLine("WORK & PROJECT EXPERIENCE")
                append(experience)
            }
        }
    }
}
