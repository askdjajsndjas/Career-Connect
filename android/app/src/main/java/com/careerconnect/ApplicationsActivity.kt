package com.careerconnect

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class ApplicationsActivity : AppCompatActivity() {

    private var activeStageFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applications)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        setupStageFilterChips()
        renderTrackedApplications()
    }

    override fun onResume() {
        super.onResume()
        renderTrackedApplications()
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
        val trackedJobs = JobRepository.getTrackedJobs(activeStageFilter)

        val cardApp1 = findViewById<CardView>(R.id.cardApp1)
        val cardApp2 = findViewById<CardView>(R.id.cardApp2)
        val extraAppsContainer = findViewById<LinearLayout>(R.id.extraAppsContainer)
        val txtEmptyApplications = findViewById<TextView>(R.id.txtEmptyApplications)

        bindStaticAppCard(
            card = cardApp1,
            logoView = findViewById(R.id.imgAppLogo1),
            titleView = findViewById(R.id.txtAppTitle1),
            companyView = findViewById(R.id.txtAppCompany1),
            statusView = findViewById(R.id.txtAppStatus1),
            job = trackedJobs.getOrNull(0)
        )

        bindStaticAppCard(
            card = cardApp2,
            logoView = findViewById(R.id.imgAppLogo2),
            titleView = findViewById(R.id.txtAppTitle2),
            companyView = findViewById(R.id.txtAppCompany2),
            statusView = findViewById(R.id.txtAppStatus2),
            job = trackedJobs.getOrNull(1)
        )

        extraAppsContainer?.removeAllViews()
        if (trackedJobs.size > 2 && extraAppsContainer != null) {
            for (job in trackedJobs.drop(2)) {
                extraAppsContainer.addView(createDynamicAppCard(job))
            }
        }

        txtEmptyApplications?.visibility = if (trackedJobs.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun bindStaticAppCard(
        card: CardView?,
        logoView: ImageView?,
        titleView: TextView?,
        companyView: TextView?,
        statusView: TextView?,
        job: JobListing?
    ) {
        if (card == null) return
        if (job == null) {
            card.visibility = View.GONE
            return
        }

        card.visibility = View.VISIBLE
        logoView?.setImageResource(job.companyLogoRes)
        titleView?.text = job.title
        companyView?.text = "${job.company} • ${job.location}"

        val displayStatus = resolveDisplayStatus(job)
        statusView?.text = displayStatus
        val statusColor = if (displayStatus.equals("Saved", ignoreCase = true)) {
            R.color.accent_teal
        } else {
            R.color.primary_blue
        }
        statusView?.setTextColor(ContextCompat.getColor(this, statusColor))

        statusView?.setOnClickListener {
            showUpdateStatusDialog(job)
        }

        card.setOnClickListener {
            val intent = Intent(this, JobDetailsActivity::class.java)
            intent.putExtra("EXTRA_JOB_ID", job.id)
            startActivity(intent)
        }
    }

    private fun createDynamicAppCard(job: JobListing): CardView {
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
            text = "${job.company} • ${job.location}"
            setTextColor(ContextCompat.getColor(this@ApplicationsActivity, R.color.text_muted))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }

        middleColumn.addView(titleView)
        middleColumn.addView(companyView)

        val displayStatus = resolveDisplayStatus(job)
        val statusColor = if (displayStatus.equals("Saved", ignoreCase = true)) {
            R.color.accent_teal
        } else {
            R.color.primary_blue
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
            setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
            setOnClickListener {
                showUpdateStatusDialog(job)
            }
        }

        rootRow.addView(logoView)
        rootRow.addView(middleColumn)
        rootRow.addView(statusView)
        cardView.addView(rootRow)

        cardView.setOnClickListener {
            val intent = Intent(this, JobDetailsActivity::class.java)
            intent.putExtra("EXTRA_JOB_ID", job.id)
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
