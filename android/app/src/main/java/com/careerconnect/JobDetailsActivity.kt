package com.careerconnect

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class JobDetailsActivity : AppCompatActivity() {

    private var currentJob: JobListing? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        val jobId = intent.getStringExtra("EXTRA_JOB_ID")
        currentJob = if (!jobId.isNullOrBlank()) {
            JobRepository.getJobById(jobId)
        } else {
            JobRepository.getAllJobs().firstOrNull()
        }

        if (currentJob == null) {
            Toast.makeText(this, "Job listing not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
    }

    override fun onResume() {
        super.onResume()
        currentJob?.let { job ->
            val refreshed = JobRepository.getJobById(job.id) ?: job
            currentJob = refreshed
            updateStatusBadge(refreshed)
            updateBookmarkIcon(findViewById(R.id.btnBookmark), refreshed.isSaved)
        }
    }

    private fun setupViews() {
        val job = currentJob ?: return

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.imgLogo)?.setImageResource(job.companyLogoRes)
        findViewById<TextView>(R.id.txtJobTitle)?.text = job.title
        findViewById<TextView>(R.id.txtCompany)?.text = job.company
        findViewById<TextView>(R.id.txtCategory)?.text = job.category
        findViewById<TextView>(R.id.txtLocation)?.text = "📍 ${job.location}"
        findViewById<TextView>(R.id.txtType)?.text = "${job.employmentType}${if (job.isRemote) " • Remote" else ""}"
        findViewById<TextView>(R.id.txtPostDate)?.text = "Posted: ${job.postDate}"
        findViewById<TextView>(R.id.txtDescription)?.text = job.description

        val reqsText = job.requirements.joinToString("\n") { "• $it" }
        findViewById<TextView>(R.id.txtRequirements)?.text = reqsText

        updateStatusBadge(job)

        val btnBookmark = findViewById<ImageButton>(R.id.btnBookmark)
        updateBookmarkIcon(btnBookmark, job.isSaved)

        btnBookmark?.setOnClickListener {
            val isSaved = JobRepository.toggleSaved(job.id)
            updateBookmarkIcon(btnBookmark, isSaved)
            updateStatusBadge(job)
            val msg = if (isSaved) "Job saved to your bookmarks" else "Job removed from bookmarks"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnApplyNow)?.setOnClickListener {
            val url = job.applicationUrl
            if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                JobRepository.updateStatus(job.id, "Application Started")
                updateStatusBadge(job)
                Toast.makeText(
                    this,
                    "Opening employer portal. Live Firestore tracking pending repository integration.",
                    Toast.LENGTH_LONG
                ).show()

                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, "No web browser installed to open this link.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Unable to launch external portal: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Application portal link is unavailable for this role.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatusBadge(job: JobListing) {
        val txtJobStatus = findViewById<TextView>(R.id.txtJobStatus) ?: return
        val displayStatus = if (job.applicationStatus == "Not Applied" && job.isSaved) {
            "Saved"
        } else {
            job.applicationStatus
        }

        if (displayStatus != "Not Applied") {
            txtJobStatus.visibility = View.VISIBLE
            txtJobStatus.text = displayStatus
            val color = when (displayStatus.lowercase()) {
                "saved" -> R.color.accent_teal
                "interview" -> R.color.primary_blue_light
                else -> R.color.primary_blue
            }
            txtJobStatus.setTextColor(ContextCompat.getColor(this, color))
        } else {
            txtJobStatus.visibility = View.GONE
        }
    }

    private fun updateBookmarkIcon(button: ImageButton?, isSaved: Boolean) {
        val colorRes = if (isSaved) R.color.primary_blue else R.color.text_muted
        button?.setColorFilter(ContextCompat.getColor(this, colorRes))
    }
}
