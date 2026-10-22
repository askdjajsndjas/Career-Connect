package com.careerconnect

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private var activeTypeFilter = "All"
    private var activeCategoryFilter = "All"
    private var currentSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupHeaderAndBanner()
        setupSearchAndFilters()
        setupBottomNavigation()
        handleIncomingFilterIntent(intent)
        updateGreeting()
        applyFilters()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingFilterIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.selectedItemId = R.id.nav_home
        updateGreeting()
        applyFilters()
    }

    private fun updateGreeting() {
        val user = FirebaseAuth.getInstance().currentUser
        val prefs = getSharedPreferences(ProfileActivity.profilePrefsName(user?.uid), Context.MODE_PRIVATE)
        val savedName = ProfileActivity.readSecurePref(prefs, "fullName", null)
        val firstName = ProfileActivity.resolveFirstName(
            savedName = savedName,
            firebaseDisplayName = user?.displayName,
            firebaseEmail = user?.email,
            defaultFirstName = "Amina"
        )
        findViewById<TextView>(R.id.txtGreeting)?.text = "Hi, $firstName! 👋"
    }

    private fun handleIncomingFilterIntent(incomingIntent: Intent?) {
        val requestedType = incomingIntent?.getStringExtra("EXTRA_FILTER_TYPE") ?: return
        activeTypeFilter = requestedType
        activeCategoryFilter = "All"
        val typeChipMap = mapOf(
            R.id.chipTypeAll to "All",
            R.id.chipTypeInternship to "Internship",
            R.id.chipTypeFullTime to "Full-time",
            R.id.chipTypePartTime to "Part-time",
            R.id.chipTypeRemote to "Remote"
        )
        updateTypeChipsUI(typeChipMap)
        updateCategoryUI()
        incomingIntent.removeExtra("EXTRA_FILTER_TYPE")
    }

    private fun setupHeaderAndBanner() {
        findViewById<ImageButton>(R.id.btnNotification)?.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        findViewById<Button>(R.id.btnExploreJobs)?.setOnClickListener {
            resetAllFilters()
        }

        findViewById<TextView>(R.id.btnSeeAllRecommended)?.setOnClickListener {
            resetAllFilters()
        }

        findViewById<TextView>(R.id.btnSeeAllCategories)?.setOnClickListener {
            activeCategoryFilter = "All"
            updateCategoryUI()
            applyFilters()
        }
    }

    private fun setupSearchAndFilters() {
        val etSearchInput = findViewById<EditText>(R.id.etSearchInput)
        etSearchInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<ImageView>(R.id.btnFilter)?.setOnClickListener {
            showCategoryFilterDialog()
        }

        val typeChipMap = mapOf(
            R.id.chipTypeAll to "All",
            R.id.chipTypeInternship to "Internship",
            R.id.chipTypeFullTime to "Full-time",
            R.id.chipTypePartTime to "Part-time",
            R.id.chipTypeRemote to "Remote"
        )

        typeChipMap.forEach { (viewId, typeValue) ->
            findViewById<TextView>(viewId)?.setOnClickListener {
                activeTypeFilter = typeValue
                updateTypeChipsUI(typeChipMap)
                applyFilters()
            }
        }

        val categoryViewMap = mapOf(
            R.id.catItTech to "IT & Tech",
            R.id.catBusiness to "Business",
            R.id.catEngineering to "Engineering",
            R.id.catMarketing to "Marketing"
        )

        categoryViewMap.forEach { (viewId, categoryName) ->
            findViewById<View>(viewId)?.setOnClickListener {
                activeCategoryFilter = if (activeCategoryFilter == categoryName) "All" else categoryName
                updateCategoryUI()
                applyFilters()
            }
        }

        findViewById<View>(R.id.catMore)?.setOnClickListener {
            resetAllFilters()
        }
    }

    private fun resetAllFilters() {
        activeTypeFilter = "All"
        activeCategoryFilter = "All"
        val etSearchInput = findViewById<EditText>(R.id.etSearchInput)
        if (!etSearchInput?.text.isNullOrEmpty()) {
            etSearchInput?.setText("")
        }
        val typeChipMap = mapOf(
            R.id.chipTypeAll to "All",
            R.id.chipTypeInternship to "Internship",
            R.id.chipTypeFullTime to "Full-time",
            R.id.chipTypePartTime to "Part-time",
            R.id.chipTypeRemote to "Remote"
        )
        updateTypeChipsUI(typeChipMap)
        updateCategoryUI()
        applyFilters()
    }

    private fun showCategoryFilterDialog() {
        val categories = arrayOf("All Categories", "IT & Tech", "Business", "Engineering", "Marketing")
        AlertDialog.Builder(this)
            .setTitle("Filter by Category")
            .setItems(categories) { _, which ->
                activeCategoryFilter = if (which == 0) "All" else categories[which]
                updateCategoryUI()
                applyFilters()
            }
            .setNeutralButton("Reset All") { _, _ ->
                resetAllFilters()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateTypeChipsUI(typeChipMap: Map<Int, String>) {
        typeChipMap.forEach { (viewId, typeValue) ->
            val chip = findViewById<TextView>(viewId) ?: return@forEach
            val isSelected = activeTypeFilter.equals(typeValue, ignoreCase = true)
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

    private fun updateCategoryUI() {
        val categoryLabelMap = mapOf(
            R.id.txtCatItTech to "IT & Tech",
            R.id.txtCatBusiness to "Business",
            R.id.txtCatEngineering to "Engineering",
            R.id.txtCatMarketing to "Marketing"
        )

        categoryLabelMap.forEach { (labelId, categoryName) ->
            val label = findViewById<TextView>(labelId) ?: return@forEach
            val isSelected = activeCategoryFilter.equals(categoryName, ignoreCase = true)
            if (isSelected) {
                label.setTextColor(ContextCompat.getColor(this, R.color.primary_blue_light))
                label.setTypeface(null, Typeface.BOLD)
            } else {
                label.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
                label.setTypeface(null, Typeface.NORMAL)
            }
        }

        val txtRecommendedTitle = findViewById<TextView>(R.id.txtRecommendedTitle)
        txtRecommendedTitle?.text = if (activeCategoryFilter == "All") {
            "Recommended for you"
        } else {
            "Recommended • $activeCategoryFilter"
        }
    }

    private fun applyFilters() {
        val results = JobRepository.filterJobs(
            query = currentSearchQuery,
            category = activeCategoryFilter,
            type = activeTypeFilter
        )
        renderJobListings(results)
    }

    private fun renderJobListings(jobs: List<JobListing>) {
        val cardJob1 = findViewById<CardView>(R.id.cardJob1)
        val cardJob2 = findViewById<CardView>(R.id.cardJob2)
        val extraJobsContainer = findViewById<LinearLayout>(R.id.extraJobsContainer)
        val txtNoJobsFound = findViewById<TextView>(R.id.txtNoJobsFound)

        bindStaticJobCard(
            card = cardJob1,
            logoView = findViewById(R.id.imgCompanyLogo1),
            titleView = findViewById(R.id.txtJobTitle1),
            companyView = findViewById(R.id.txtCompanyName1),
            bookmarkBtn = findViewById(R.id.btnBookmark1),
            tagsLayout = findViewById(R.id.tagsLayout1),
            postDateView = findViewById(R.id.txtPostDate1),
            job = jobs.getOrNull(0)
        )

        bindStaticJobCard(
            card = cardJob2,
            logoView = findViewById(R.id.imgCompanyLogo2),
            titleView = findViewById(R.id.txtJobTitle2),
            companyView = findViewById(R.id.txtCompanyName2),
            bookmarkBtn = findViewById(R.id.btnBookmark2),
            tagsLayout = findViewById(R.id.tagsLayout2),
            postDateView = findViewById(R.id.txtPostDate2),
            job = jobs.getOrNull(1)
        )

        extraJobsContainer?.removeAllViews()
        if (jobs.size > 2 && extraJobsContainer != null) {
            for (job in jobs.drop(2)) {
                extraJobsContainer.addView(createDynamicJobCard(job))
            }
        }

        txtNoJobsFound?.visibility = if (jobs.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun bindStaticJobCard(
        card: CardView?,
        logoView: ImageView?,
        titleView: TextView?,
        companyView: TextView?,
        bookmarkBtn: ImageButton?,
        tagsLayout: LinearLayout?,
        postDateView: TextView?,
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
        companyView?.text = job.company
        postDateView?.text = job.postDate

        if (tagsLayout != null && tagsLayout.childCount >= 2) {
            (tagsLayout.getChildAt(0) as? TextView)?.text = job.employmentType
            val locationLabel = if (job.isRemote) "Remote" else job.location.substringBefore(",")
            (tagsLayout.getChildAt(1) as? TextView)?.text = locationLabel
        }

        updateBookmarkIcon(bookmarkBtn, job.isSaved)
        bookmarkBtn?.setOnClickListener {
            val isSaved = JobRepository.toggleSaved(job.id)
            updateBookmarkIcon(bookmarkBtn, isSaved)
            val msg = if (isSaved) "Job saved to your bookmarks" else "Job removed from bookmarks"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        card.setOnClickListener {
            val intent = Intent(this, JobDetailsActivity::class.java)
            intent.putExtra("EXTRA_JOB_ID", job.id)
            startActivity(intent)
        }
    }

    private fun createDynamicJobCard(job: JobListing): CardView {
        val cardView = CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(12)
            }
            radius = dpToPx(16).toFloat()
            cardElevation = dpToPx(2).toFloat()
            setCardBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.white))
        }

        val rootRow = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            val pad = dpToPx(16)
            setPadding(pad, pad, pad, pad)
        }

        val logoView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(48), dpToPx(48))
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
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_dark))
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
            text = job.company
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_muted))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
        }

        val tagsRow = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(12)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val typePill = createTagPill(job.employmentType, hasEndMargin = true)
        val locationLabel = if (job.isRemote) "Remote" else job.location.substringBefore(",")
        val locationPill = createTagPill(locationLabel, hasEndMargin = false)
        tagsRow.addView(typePill)
        tagsRow.addView(locationPill)

        middleColumn.addView(titleView)
        middleColumn.addView(companyView)
        middleColumn.addView(tagsRow)

        val rightColumn = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
        }

        val bookmarkBtn = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(32), dpToPx(32))
            val outValue = TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            setBackgroundResource(outValue.resourceId)
            setImageResource(R.drawable.ic_bookmark)
            contentDescription = "Bookmark"
        }
        updateBookmarkIcon(bookmarkBtn, job.isSaved)
        bookmarkBtn.setOnClickListener {
            val isSaved = JobRepository.toggleSaved(job.id)
            updateBookmarkIcon(bookmarkBtn, isSaved)
            val msg = if (isSaved) "Job saved to your bookmarks" else "Job removed from bookmarks"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 0, 1f)
        }

        val postDateView = TextView(this).apply {
            text = job.postDate
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_muted))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
        }

        rightColumn.addView(bookmarkBtn)
        rightColumn.addView(spacer)
        rightColumn.addView(postDateView)

        rootRow.addView(logoView)
        rootRow.addView(middleColumn)
        rootRow.addView(rightColumn)
        cardView.addView(rootRow)

        cardView.setOnClickListener {
            val intent = Intent(this, JobDetailsActivity::class.java)
            intent.putExtra("EXTRA_JOB_ID", job.id)
            startActivity(intent)
        }

        return cardView
    }

    private fun createTagPill(label: String, hasEndMargin: Boolean): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (hasEndMargin) marginEnd = dpToPx(6)
            }
            setBackgroundResource(R.drawable.bg_tag_pill)
            text = label
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_muted))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setPadding(dpToPx(10), dpToPx(4), dpToPx(10), dpToPx(4))
        }
    }

    private fun updateBookmarkIcon(button: ImageButton?, isSaved: Boolean) {
        val colorRes = if (isSaved) R.color.primary_blue else R.color.text_muted
        button?.setColorFilter(ContextCompat.getColor(this, colorRes))
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    private fun setupBottomNavigation() {
        NavigationHelper.setupBottomNavigation(this, R.id.nav_home)
    }
}
