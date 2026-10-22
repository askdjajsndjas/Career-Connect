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
import com.google.firebase.firestore.ListenerRegistration

class ApplicationsActivity : AppCompatActivity() {

    private var activeStageFilter = "All"
    private var firestoreRegistration: ListenerRegistration? = null
    private var firestoreApplications: List<JobApplication> = emptyList()
    private var firestoreError: String? = null

    data class TrackedDisplayItem(
        val jobId: String,
        val title: String,
        val company: String,
        val location: String,
        val employmentType: String,
        val logoRes: Int,
        val statusRaw: String,
        val statusLabel: String,
        val isFirestore: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applications)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnBrowseJobs)?.setOnClickListener {
            val intent = Intent(this, JobsActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnRetryApplications)?.setOnClickListener {
            firestoreError = null
            findViewById<View>(R.id.layoutErrorApplications)?.visibility = View.GONE
            subscribeToFirestoreApplications()
        }

        setupStageFilterChips()
        setupStatCards()
        setupBottomNav()
        subscribeToFirestoreApplications()
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.selectedItemId = R.id.nav_applications
        renderTrackedApplications()
    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreRegistration?.remove()
        firestoreRegistration = null
    }

    private fun subscribeToFirestoreApplications() {
        firestoreRegistration?.remove()
        firestoreRegistration = ApplicationRepository.listenToMyApplications(
            onChange = { apps ->
                firestoreError = null
                firestoreApplications = apps
                for (app in apps) {
                    JobRepository.updateStatus(app.jobId, app.status)
                }
                renderTrackedApplications()
            },
            onError = { errMsg ->
                firestoreError = errMsg
                renderTrackedApplications()
            }
        )
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
        val allItems = mutableListOf<TrackedDisplayItem>()
        val firestoreJobIds = mutableSetOf<String>()

        for (app in firestoreApplications) {
            firestoreJobIds.add(app.jobId)
            val job = JobRepository.getJobById(app.jobId)
            val title = if (app.jobTitle.isNotBlank()) app.jobTitle else (job?.title ?: "Application")
            val company = if (app.company.isNotBlank()) app.company else (job?.company ?: "Company")
            val location = job?.location ?: "South Africa"
            val employmentType = job?.employmentType ?: "Full-time"
            val logoRes = job?.companyLogoRes ?: R.drawable.ic_company_abc

            val statusLabel = when (app.status) {
                ApplicationRepository.INTERVIEW -> "Interview"
                ApplicationRepository.REJECTED -> "Rejected"
                ApplicationRepository.ARCHIVED -> "Archived"
                else -> "Pending"
            }

            allItems.add(
                TrackedDisplayItem(
                    jobId = app.jobId,
                    title = title,
                    company = company,
                    location = location,
                    employmentType = employmentType,
                    logoRes = logoRes,
                    statusRaw = app.status,
                    statusLabel = statusLabel,
                    isFirestore = true
                )
            )
        }

        // Add local saved jobs (bookmarks) that are not already tracked in Firestore
        val savedJobs = JobRepository.getTrackedJobs("Saved")
        for (job in savedJobs) {
            if (!firestoreJobIds.contains(job.id)) {
                allItems.add(
                    TrackedDisplayItem(
                        jobId = job.id,
                        title = job.title,
                        company = job.company,
                        location = job.location,
                        employmentType = job.employmentType,
                        logoRes = job.companyLogoRes,
                        statusRaw = "Saved",
                        statusLabel = "Saved",
                        isFirestore = false
                    )
                )
            }
        }

        // Summary metric counts: Saved (local bookmarks), Applied (Firestore PENDING), Interview (Firestore INTERVIEW)
        val savedCount = savedJobs.size
        val appliedCount = firestoreApplications.count {
            it.status.equals(ApplicationRepository.PENDING, ignoreCase = true) ||
                it.status.equals("Applied", ignoreCase = true)
        }
        val interviewCount = firestoreApplications.count {
            it.status.equals(ApplicationRepository.INTERVIEW, ignoreCase = true) ||
                it.status.equals("Interview", ignoreCase = true)
        }

        findViewById<TextView>(R.id.txtCountSaved)?.text = savedCount.toString()
        findViewById<TextView>(R.id.txtCountApplied)?.text = appliedCount.toString()
        findViewById<TextView>(R.id.txtCountInterview)?.text = interviewCount.toString()

        val filteredItems = when (activeStageFilter) {
            "All" -> allItems
            "Saved" -> allItems.filter { !it.isFirestore || JobRepository.getJobById(it.jobId)?.isSaved == true }
            "Applied" -> allItems.filter {
                it.statusRaw.equals(ApplicationRepository.PENDING, ignoreCase = true) ||
                    it.statusRaw.equals("Applied", ignoreCase = true) ||
                    it.statusRaw.equals("Application Started", ignoreCase = true)
            }
            "Interview" -> allItems.filter {
                it.statusRaw.equals(ApplicationRepository.INTERVIEW, ignoreCase = true) ||
                    it.statusRaw.equals("Interview", ignoreCase = true)
            }
            "Rejected" -> allItems.filter {
                it.statusRaw.equals(ApplicationRepository.REJECTED, ignoreCase = true)
            }
            "Archived" -> allItems.filter {
                it.statusRaw.equals(ApplicationRepository.ARCHIVED, ignoreCase = true)
            }
            else -> allItems
        }

        val container = findViewById<LinearLayout>(R.id.appsListContainer)
        val emptyLayout = findViewById<View>(R.id.layoutEmptyApplications)
        val errorLayout = findViewById<View>(R.id.layoutErrorApplications)

        container?.removeAllViews()

        if (firestoreError != null && firestoreApplications.isEmpty()) {
            errorLayout?.visibility = View.VISIBLE
            emptyLayout?.visibility = View.GONE
            container?.visibility = View.GONE
            findViewById<TextView>(R.id.txtErrorApplicationsMessage)?.text =
                "Could not sync your applications: $firestoreError"
        } else {
            errorLayout?.visibility = View.GONE
            if (filteredItems.isEmpty()) {
                emptyLayout?.visibility = View.VISIBLE
                container?.visibility = View.GONE
            } else {
                emptyLayout?.visibility = View.GONE
                container?.visibility = View.VISIBLE
                for (item in filteredItems) {
                    container?.addView(createAppCard(item))
                }
            }
        }
    }

    private fun createAppCard(item: TrackedDisplayItem): CardView {
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
            setImageResource(item.logoRes)
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
            text = item.title
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
            text = "${item.company} • ${item.location} • ${item.employmentType}"
            setTextColor(ContextCompat.getColor(this@ApplicationsActivity, R.color.text_muted))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        }

        middleColumn.addView(titleView)
        middleColumn.addView(companyView)

        val statusColor = when (item.statusLabel.lowercase()) {
            "saved" -> R.color.accent_teal
            "interview" -> R.color.primary_blue_light
            "rejected", "archived" -> R.color.text_muted
            else -> R.color.primary_blue
        }

        val statusView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundResource(R.drawable.bg_tag_pill)
            text = item.statusLabel
            setTextColor(ContextCompat.getColor(this@ApplicationsActivity, statusColor))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTypeface(null, Typeface.BOLD)
            setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5))
            setOnClickListener {
                showStatusDetailDialog(item)
            }
        }

        rootRow.addView(logoView)
        rootRow.addView(middleColumn)
        rootRow.addView(statusView)
        cardView.addView(rootRow)

        cardView.setOnClickListener {
            val intent = Intent(this, JobDetailsActivity::class.java).apply {
                putExtra("EXTRA_JOB_ID", item.jobId)
            }
            startActivity(intent)
        }

        return cardView
    }

    private fun showStatusDetailDialog(item: TrackedDisplayItem) {
        if (item.isFirestore) {
            AlertDialog.Builder(this)
                .setTitle("${item.title} — ${item.company}")
                .setMessage("Status: ${item.statusLabel}\n\nTracked live via Cloud Firestore. Any status updates from the employer will reflect here.")
                .setPositiveButton("View Job Details") { _, _ ->
                    val intent = Intent(this, JobDetailsActivity::class.java).apply {
                        putExtra("EXTRA_JOB_ID", item.jobId)
                    }
                    startActivity(intent)
                }
                .setNegativeButton("Close", null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("${item.title} — ${item.company}")
                .setMessage("This role is bookmarked in your Saved list.")
                .setPositiveButton("View Job & Apply") { _, _ ->
                    val intent = Intent(this, JobDetailsActivity::class.java).apply {
                        putExtra("EXTRA_JOB_ID", item.jobId)
                    }
                    startActivity(intent)
                }
                .setNeutralButton("Remove Bookmark") { _, _ ->
                    JobRepository.toggleSaved(item.jobId)
                    renderTrackedApplications()
                    Toast.makeText(this, "Removed from bookmarks", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
