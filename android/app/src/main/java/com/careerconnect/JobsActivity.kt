package com.careerconnect

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class JobsActivity : AppCompatActivity() {

    private var activeCategoryTag: String? = null
    private var currentSearchQuery: String? = null
    private var isFetching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jobs)

        setupTopBar()
        setupAttribution()
        setupSearch()
        setupCategoryChips()
        setupErrorAndEmptyButtons()

        loadJobs()
    }

    private fun setupTopBar() {
        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnRefreshJobs)?.setOnClickListener { loadJobs() }
    }

    private fun setupAttribution() {
        val openRemotive = View.OnClickListener {
            openUrl("https://remotive.com")
        }
        findViewById<View>(R.id.cardRemotiveAttribution)?.setOnClickListener(openRemotive)
        findViewById<View>(R.id.btnVisitRemotive)?.setOnClickListener(openRemotive)
    }

    private fun setupSearch() {
        val edtSearch = findViewById<EditText>(R.id.edtSearchJobs) ?: return
        val btnClear = findViewById<TextView>(R.id.btnClearSearch)

        edtSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_GO ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                currentSearchQuery = edtSearch.text.toString().trim().ifEmpty { null }
                loadJobs()
                true
            } else {
                false
            }
        }

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                val query = s?.toString()?.trim()
                btnClear?.visibility = if (!query.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnClear?.setOnClickListener {
            edtSearch.text.clear()
            currentSearchQuery = null
            loadJobs()
        }
    }

    private fun setupCategoryChips() {
        val chipMapping = listOf(
            R.id.chipCatAll to null,
            R.id.chipCatDev to "software-dev",
            R.id.chipCatDevOps to "devops-sysadmin",
            R.id.chipCatData to "data",
            R.id.chipCatDesign to "design",
            R.id.chipCatMarketing to "marketing"
        )

        for ((chipId, categorySlug) in chipMapping) {
            findViewById<TextView>(chipId)?.setOnClickListener {
                activeCategoryTag = categorySlug
                updateChipsUI(chipMapping)
                loadJobs()
            }
        }
    }

    private fun updateChipsUI(chipMapping: List<Pair<Int, String?>>) {
        for ((chipId, slug) in chipMapping) {
            val chip = findViewById<TextView>(chipId) ?: continue
            val isSelected = (slug == activeCategoryTag)
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

    private fun setupErrorAndEmptyButtons() {
        findViewById<Button>(R.id.btnRetryJobs)?.setOnClickListener {
            loadJobs()
        }

        findViewById<Button>(R.id.btnResetJobsFilter)?.setOnClickListener {
            activeCategoryTag = null
            currentSearchQuery = null
            findViewById<EditText>(R.id.edtSearchJobs)?.text?.clear()
            setupCategoryChips()
            loadJobs()
        }
    }

    private fun loadJobs() {
        if (isFetching) return
        isFetching = true

        val layoutLoading = findViewById<View>(R.id.layoutLoadingJobs)
        val layoutError = findViewById<View>(R.id.layoutErrorJobs)
        val layoutEmpty = findViewById<View>(R.id.layoutEmptyJobs)
        val scrollView = findViewById<View>(R.id.scrollViewJobs)
        val container = findViewById<LinearLayout>(R.id.jobsContainer)
        val txtCountHeader = findViewById<TextView>(R.id.txtJobCountHeader)

        layoutLoading?.visibility = View.VISIBLE
        layoutError?.visibility = View.GONE
        layoutEmpty?.visibility = View.GONE
        scrollView?.visibility = View.GONE
        container?.removeAllViews()

        JobApi.fetchJobs(
            category = activeCategoryTag,
            search = currentSearchQuery,
            limit = 40
        ) { result ->
            runOnUiThread {
                isFetching = false
                layoutLoading?.visibility = View.GONE

                result.onSuccess { jobs ->
                    if (jobs.isEmpty()) {
                        layoutEmpty?.visibility = View.VISIBLE
                        scrollView?.visibility = View.GONE
                        txtCountHeader?.text = "0 remote openings found"
                    } else {
                        layoutEmpty?.visibility = View.GONE
                        scrollView?.visibility = View.VISIBLE
                        txtCountHeader?.text = "Showing ${jobs.size} remote openings via Remotive"

                        for (job in jobs) {
                            val card = createJobCard(job)
                            container?.addView(card)
                        }
                    }
                }.onFailure { err ->
                    layoutError?.visibility = View.VISIBLE
                    scrollView?.visibility = View.GONE
                    layoutEmpty?.visibility = View.GONE
                    findViewById<TextView>(R.id.txtErrorJobsMessage)?.text =
                        err.localizedMessage ?: "Failed to retrieve public job feed from Remotive."
                    txtCountHeader?.text = "Error loading remote jobs"
                }
            }
        }
    }

    private fun createJobCard(job: RemoteJob): CardView {
        val card = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
            radius = dpToPx(14).toFloat()
            cardElevation = dpToPx(2).toFloat()
            maxCardElevation = dpToPx(4).toFloat()
            setCardBackgroundColor(ContextCompat.getColor(this@JobsActivity, R.color.white))
            isClickable = true
            isFocusable = true
            foreground = ContextCompat.getDrawable(this@JobsActivity, android.R.drawable.list_selector_background)
        }

        val cardContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dpToPx(16)
            setPadding(pad, pad, pad, pad)
        }

        // Job Title
        val txtTitle = TextView(this).apply {
            text = job.title ?: "Remote Position"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@JobsActivity, R.color.text_dark))
            setLineSpacing(dpToPx(2).toFloat(), 1f)
        }
        cardContent.addView(txtTitle)

        // Company
        val txtCompany = TextView(this).apply {
            text = job.companyName ?: "Remotive Partner"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@JobsActivity, R.color.primary_blue))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(4)
            }
        }
        cardContent.addView(txtCompany)

        // Tags Row (Location + Category / Job Type)
        val tagsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(10)
            }
        }

        val loc = job.location?.ifBlank { null } ?: "Worldwide Remote"
        tagsRow.addView(createTagPill("📍 $loc", R.color.text_muted, "#F1F5F9"))

        job.category?.let { cat ->
            tagsRow.addView(createTagPill(cat, R.color.accent_teal, "#E6FFFA"))
        }

        job.jobType?.let { jt ->
            tagsRow.addView(createTagPill(jt.replace("_", " "), R.color.primary_blue, "#EFF6FF"))
        }

        cardContent.addView(tagsRow)

        // Bottom action row: Link prompt
        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(12)
            }
        }

        val txtApplyPrompt = TextView(this).apply {
            text = "View & Apply on Remotive  ↗"
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@JobsActivity, R.color.primary_blue))
        }
        actionRow.addView(txtApplyPrompt)

        cardContent.addView(actionRow)

        card.addView(cardContent)

        card.setOnClickListener {
            val url = job.url
            if (!url.isNullOrBlank()) {
                openUrl(url)
            } else {
                Toast.makeText(this, "No external posting URL available", Toast.LENGTH_SHORT).show()
            }
        }

        return card
    }

    private fun createTagPill(label: String, textColorRes: Int, bgHex: String): TextView {
        return TextView(this).apply {
            text = label
            textSize = 11f
            setTextColor(ContextCompat.getColor(this@JobsActivity, textColorRes))
            setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3))
            setBackgroundColor(android.graphics.Color.parseColor(bgHex))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dpToPx(6)
            }
        }
    }

    private fun openUrl(url: String) {
        val trimmed = url.trim()
        val targetUri = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            Uri.parse("https://$trimmed")
        } else {
            Uri.parse(trimmed)
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, targetUri)
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No web browser found to open link", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
