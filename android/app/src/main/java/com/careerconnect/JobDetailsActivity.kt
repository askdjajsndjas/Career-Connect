package com.careerconnect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class JobDetailsActivity : AppCompatActivity() {

    private var currentJob: JobListing? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        val jobId = intent.getStringExtra("EXTRA_JOB_ID") ?: "job_1"
        currentJob = JobRepository.getJobById(jobId) ?: JobRepository.getAllJobs().first()

        setupViews()
    }

    private fun setupViews() {
        val job = currentJob ?: return

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.imgLogo)?.setImageResource(job.companyLogoRes)
        findViewById<TextView>(R.id.txtJobTitle)?.text = job.title
        findViewById<TextView>(R.id.txtCompany)?.text = job.company
        findViewById<TextView>(R.id.txtLocation)?.text = job.location
        findViewById<TextView>(R.id.txtType)?.text = "${job.employmentType}${if (job.isRemote) " • Remote" else ""}"
        findViewById<TextView>(R.id.txtDescription)?.text = job.description
        
        val reqsText = job.requirements.joinToString("\n") { "• $it" }
        findViewById<TextView>(R.id.txtRequirements)?.text = reqsText

        val btnBookmark = findViewById<ImageButton>(R.id.btnBookmark)
        updateBookmarkIcon(btnBookmark, job.isSaved)

        btnBookmark?.setOnClickListener {
            val isSaved = JobRepository.toggleSaved(job.id)
            updateBookmarkIcon(btnBookmark, isSaved)
            val msg = if (isSaved) "Job saved to your bookmarks" else "Job removed from bookmarks"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnApplyNow)?.setOnClickListener {
            val url = job.applicationUrl
            if (url.isNotEmpty()) {
                JobRepository.updateStatus(job.id, "Application Started")
                Toast.makeText(this, "Opening employer's external site...", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Application link unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBookmarkIcon(button: ImageButton?, isSaved: Boolean) {
        if (isSaved) {
            button?.setColorFilter(resources.getColor(R.color.primary_blue, theme))
        } else {
            button?.setColorFilter(resources.getColor(R.color.text_muted, theme))
        }
    }
}
