package com.careerconnect

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class ApplicationsActivity : AppCompatActivity() {

    private var activeStageFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applications)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnBrowseJobs)?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        setupStageFilterChips()
        setupStatCards()
        setupBottomNav()
        renderTrackedApplications()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.selectedItemId = R.id.nav_applications
        renderTrackedApplications()
    }

    private fun setupBottomNav() {
        NavigationHelper.setupBottomNavigation(this, R.id.nav_applications)
    }

    private fun setupStageFilterChips() {
        val chipMap = mapOf(
            R.id.chipAllApps to "All",
            R.id.chipSavedApps to "Saved",
            R.id.chipAppliedApps to "Applied",
            R.id.chipInterviewApps to "Interview"
        )

        chipMap.forEach { (chipId, stageName) ->
            findViewById<TextView>(chipId)?.setOnClickListener {
                activeStageFilter = stageName
                updateStageChipsUI(chipMap)
                renderTrackedApplications()
            }
        }
    }

    private fun setupStatCards() {
        findViewById<View>(R.id.cardStatSaved)?.setOnClickListener {
            activeStageFilter = "Saved"
            updateStageChipsUI(mapOf(
                R.id.chipAllApps to "All",
                R.id.chipSavedApps to "Saved",
                R.id.chipAppliedApps to "Applied",
                R.id.chipInterviewApps to "Interview"
            ))
            renderTrackedApplications()
        }

        findViewById<View>(R.id.cardStatApplied)?.setOnClickListener {
            activeStageFilter = "Applied"
            updateStageChipsUI(mapOf(
                R.id.chipAllApps to "All",
                R.id.chipSavedApps to "Saved",
                R.id.chipAppliedApps to "Applied",
                R.id.chipInterviewApps to "Interview"
            ))
            renderTrackedApplications()
        }

        findViewById<View>(R.id.cardStatInterview)?.setOnClickListener {
            activeStageFilter = "Interview"
            updateStageChipsUI(mapOf(
                R.id.chipAllApps to "All",
                R.id.chipSavedApps to "Saved",
                R.id.chipAppliedApps to "Applied",
                R.id.chipInterviewApps to "Interview"
            ))
            renderTrackedApplications()
        }
    }

    private fun updateStageChipsUI(chipMap: Map<Int, String>) {
        chipMap.forEach { (chipId, stageName) ->
            val chip = findViewById<TextView>(chipId) ?: return@forEach
            val isSelected = activeStageFilter.equals(stageName, ignoreCase = true)
            if (isSelected) {
                chip.setBackgroundResource(R.drawable.bg_chip_active)
                chip.setTextColor(ContextCompat.getColor(this, R.color.white))
                chip.setTypeface(null, Typeface.BOLD)
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_inactive)
                chip.setTextColor(ContextCompat.getColor(this, R.color.text_muted))
                chip.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    private fun renderTrackedApplications() {
        // Update summary metrics from JobRepository
        val savedCount = JobRepository.getTrackedJobs("Saved").size
        val appliedCount = JobRepository.getTrackedJobs("Applied").size
        val interviewCount = JobRepository.getTrackedJobs("Interview").size

        findViewById<TextView>(R.id.txtCountSaved)?.text = savedCount.toString()
        findViewById<TextView>(R.id.txtCountApplied)?.text = appliedCount.toString()
        findViewById<TextView>(R.id.txtCountInterview)?.text = interviewCount.toString()

        val trackedJobs = JobRepository.getTrackedJobs(activeStageFilter)
        val container = findViewById<LinearLayout>(R.id.appsListContainer)
        val emptyLayout = findViewById<View>(R.id.layoutEmptyApplications)

        container?.removeAllViews()

        if (trackedJobs.isEmpty()) {
            emptyLayout?.visibility = View.VISIBLE
            container?.visibility = View.GONE
        } else {
            emptyLayout?.visibility = View.GONE
            container?.visibility = View.VISIBLE
            for (job in trackedJobs) {
                container?.addView(createAppCard(job))
            }
        }
    }

    private fun createAppCard(job: JobListing): CardView {
        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
            radius = dpToPx(14).toFloat()
            cardElevation = dpToPx(2).toFloat()
            setCardBackgroundColor(ContextCompat.getColor(this@ApplicationsActivity, R.color.white))
        }

        val rootRow = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val pad = dpToPx(16)
            setPadding(pad, pad, pad, pad)
        }

        val logoView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(44), dpToPx(44))
            setImageResource(job.companyLogoRes)
            contentDescription = "Company Logo"
        }

        val middleColumn = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dpToPx(12)
                marginEnd = dpToPx(8)
            }
            orientation = LinearLayout.VERTICAL
        }

        val titleView = TextView(this).apply {
            text = job.title
            setTextColor(ContextCompat.getColor(this@ApplicationsActivity, R.color.text_dark))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTypeface(null, Typeface.BOLD)
        }

        val companyView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(2)
            }
            text = "${job.company} • ${job.location} • ${job.employmentType}"
            setTextColor(ContextCompat.getColor(this@ApplicationsActivity, R.color.text_muted))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }

        middleColumn.addView(titleView)
        middleColumn.addView(companyView)

        val displayStatus = resolveDisplayStatus(job)
        val statusColor = when (displayStatus.lowercase()) {
            "saved" -> R.color.accent_teal
            "interview" -> R.color.primary_blue_light
            else -> R.color.primary_blue
        }

        val statusView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundResource(R.drawable.bg_tag_pill)
            text = displayStatus
            setTextColor(ContextCompat.getColor(this@ApplicationsActivity, statusColor))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTypeface(null, Typeface.BOLD)
            setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5))
            setOnClickListener {
                showUpdateStatusDialog(job)
            }
        }

        rootRow.addView(logoView)
        rootRow.addView(middleColumn)
        rootRow.addView(statusView)
        cardView.addView(rootRow)

        cardView.setOnClickListener {
            val intent = Intent(this, JobDetailsActivity::class.java).apply {
                putExtra("EXTRA_JOB_ID", job.id)
            }
            startActivity(intent)
        }

        return cardView
    }

    private fun resolveDisplayStatus(job: JobListing): String {
        return if (job.applicationStatus == "Not Applied" && job.isSaved) {
            "Saved"
        } else {
            job.applicationStatus
        }
    }

    private fun showUpdateStatusDialog(job: JobListing) {
        val options = arrayOf("Saved", "Application Started", "Applied", "Interview", "Remove from Tracked")
        AlertDialog.Builder(this)
            .setTitle("Update Status: ${job.title}")
            .setItems(options) { _, which ->
                if (which == 4) {
                    JobRepository.updateStatus(job.id, "Not Applied")
                    Toast.makeText(this, "Removed from tracked applications", Toast.LENGTH_SHORT).show()
                } else {
                    val newStatus = options[which]
                    JobRepository.updateStatus(job.id, newStatus)
                    Toast.makeText(this, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                }
                renderTrackedApplications()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
