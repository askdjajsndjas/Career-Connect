package com.careerconnect

import android.content.Context
import android.content.Intent
import android.graphics.Color
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

    private val pathwayChipsMapping by lazy {
        listOf(
            R.id.chipPathwayAll to "All",
            R.id.chipPathwaySoftware to JobRepository.PATHWAY_SOFTWARE,
            R.id.chipPathwayData to JobRepository.PATHWAY_DATA,
            R.id.chipPathwayCloud to JobRepository.PATHWAY_NETWORKING,
            R.id.chipPathwaySecurity to JobRepository.PATHWAY_SECURITY,
            R.id.chipPathwayRobotics to JobRepository.PATHWAY_ROBOTICS,
            R.id.chipPathwaySupport to JobRepository.PATHWAY_IT_SUPPORT,
            R.id.chipPathwayBusiness to JobRepository.PATHWAY_BUSINESS,
            R.id.chipPathwayMarketing to JobRepository.PATHWAY_MARKETING
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StatusNotifier.start(this)

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
            defaultFirstName = "Student"
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
            startActivity(Intent(this, JobsActivity::class.java))
        }

        findViewById<TextView>(R.id.btnSeeAllRecommended)?.setOnClickListener {
            startActivity(Intent(this, JobsActivity::class.java))
        }

        findViewById<TextView>(R.id.btnSeeAllCategories)?.setOnClickListener {
            showCategoryFilterDialog()
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

        // 1. Employment Type Chips
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

        // 2. Career Pathway & Major Chips
        pathwayChipsMapping.forEach { (viewId, pathwayValue) ->
            findViewById<TextView>(viewId)?.setOnClickListener {
                activeCategoryFilter = if (activeCategoryFilter == pathwayValue && pathwayValue != "All") "All" else pathwayValue
                updatePathwayChipsUI()
                updateCategoryUI()
                applyFilters()
            }
        }

        // 3. Top Categories Icon Row (mapped cleanly to our pathways)
        val categoryViewMap = mapOf(
            R.id.catItTech to JobRepository.PATHWAY_SOFTWARE,
            R.id.catEngineering to JobRepository.PATHWAY_ROBOTICS,
            R.id.catBusiness to JobRepository.PATHWAY_DATA,
            R.id.catMarketing to JobRepository.PATHWAY_MARKETING
        )

        categoryViewMap.forEach { (viewId, pathwayName) ->
            findViewById<View>(viewId)?.setOnClickListener {
                activeCategoryFilter = if (activeCategoryFilter == pathwayName) "All" else pathwayName
                updatePathwayChipsUI()
                updateCategoryUI()
                applyFilters()
            }
        }

        findViewById<View>(R.id.catMore)?.setOnClickListener {
            showCategoryFilterDialog()
        }
    }

    private fun resetAllFilters() {
        activeTypeFilter = "All"
        activeCategoryFilter = "All"
        currentSearchQuery = ""
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
        updatePathwayChipsUI()
        updateCategoryUI()
        applyFilters()
    }

    private fun showCategoryFilterDialog() {
        val pathways = arrayOf(
            "All Majors & Pathways",
            JobRepository.PATHWAY_SOFTWARE,
            JobRepository.PATHWAY_DATA,
            JobRepository.PATHWAY_NETWORKING,
            JobRepository.PATHWAY_SECURITY,
            JobRepository.PATHWAY_ROBOTICS,
            JobRepository.PATHWAY_IT_SUPPORT,
            JobRepository.PATHWAY_BUSINESS,
            JobRepository.PATHWAY_MARKETING
        )

        AlertDialog.Builder(this)
            .setTitle("Select Degree / Career Pathway")
            .setItems(pathways) { _, which ->
                activeCategoryFilter = if (which == 0) "All" else pathways[which]
                updatePathwayChipsUI()
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

    private fun updatePathwayChipsUI() {
        pathwayChipsMapping.forEach { (viewId, pathwayValue) ->
            val chip = findViewById<TextView>(viewId) ?: return@forEach
            val isSelected = if (pathwayValue == "All") {
                activeCategoryFilter == "All"
            } else {
                activeCategoryFilter.equals(pathwayValue, ignoreCase = true)
            }
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
            R.id.txtCatItTech to JobRepository.PATHWAY_SOFTWARE,
            R.id.txtCatEngineering to JobRepository.PATHWAY_ROBOTICS,
            R.id.txtCatBusiness to JobRepository.PATHWAY_DATA,
            R.id.txtCatMarketing to JobRepository.PATHWAY_MARKETING
        )

        categoryLabelMap.forEach { (labelId, pathwayName) ->
            val label = findViewById<TextView>(labelId) ?: return@forEach
            val isSelected = activeCategoryFilter.equals(pathwayName, ignoreCase = true)
            if (isSelected) {
                label.setTextColor(ContextCompat.getColor(this, R.color.primary_blue_light))
                label.setTypeface(null, Typeface.BOLD)
            } else {
                label.setTextColor(ContextCompat.getColor(this, R.color.text_dark))
                label.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    private fun applyFilters() {
        val results = if (activeCategoryFilter == "All" && currentSearchQuery.isEmpty() && activeTypeFilter == "All") {
            val user = FirebaseAuth.getInstance().currentUser
            val prefs = getSharedPreferences(ProfileActivity.profilePrefsName(user?.uid), Context.MODE_PRIVATE)
            val qual = ProfileActivity.readSecurePref(prefs, "qualification", null)
            val skills = ProfileActivity.readSecurePref(prefs, "skills", null)
            val interest = ProfileActivity.readSecurePref(prefs, "careerInterest", null)
            JobRepository.getPersonalizedRecommendations(qual, skills, interest)
        } else {
            JobRepository.filterJobs(
                query = currentSearchQuery,
                category = activeCategoryFilter,
                type = activeTypeFilter
            )
        }

        val txtRecommendedTitle = findViewById<TextView>(R.id.txtRecommendedTitle)
        if (activeCategoryFilter == "All") {
            val user = FirebaseAuth.getInstance().currentUser
            val prefs = getSharedPreferences(ProfileActivity.profilePrefsName(user?.uid), Context.MODE_PRIVATE)
            val qual = ProfileActivity.readSecurePref(prefs, "qualification", null)
            val qualLabel = if (!qual.isNullOrBlank()) " • $qual" else ""
            txtRecommendedTitle?.text = "Recommended for you$qualLabel (${results.size} SA listings)"
        } else {
            val shortName = shortenCategoryName(activeCategoryFilter)
            txtRecommendedTitle?.text = "Recommended • $shortName (${results.size})"
        }

        renderJobListings(results)
    }

    private fun shortenCategoryName(fullName: String): String {
        return when (fullName) {
            JobRepository.PATHWAY_SOFTWARE -> "Software Dev"
            JobRepository.PATHWAY_DATA -> "Data Science"
            JobRepository.PATHWAY_NETWORKING -> "Cloud & Networks"
            JobRepository.PATHWAY_SECURITY -> "Cybersecurity"
            JobRepository.PATHWAY_ROBOTICS -> "Robotics & Automation"
            JobRepository.PATHWAY_IT_SUPPORT -> "IT Support"
            JobRepository.PATHWAY_BUSINESS -> "Business"
            JobRepository.PATHWAY_MARKETING -> "Marketing"
            else -> fullName
        }
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

        if (jobs.isEmpty()) {
            txtNoJobsFound?.visibility = View.VISIBLE
            val filterDesc = if (activeCategoryFilter != "All") " in '${shortenCategoryName(activeCategoryFilter)}'" else ""
            val queryDesc = if (currentSearchQuery.isNotEmpty()) " matching '$currentSearchQuery'" else ""
            txtNoJobsFound?.text = "No South African opportunities found$filterDesc$queryDesc.\nTap 'All Majors' or reset filters to browse all available opportunities."
        } else {
            txtNoJobsFound?.visibility = View.GONE
        }
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
            val locationLabel = if (job.isRemote) "Remote • SA" else job.location.substringBefore(",")
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
                topMargin = dpToPx(10)
            }
            orientation = LinearLayout.HORIZONTAL
        }

        val typePill = createTagPill(job.employmentType, hasEndMargin = true)
        val locationLabel = if (job.isRemote) "Remote • SA" else "📍 " + job.location.substringBefore(",")
        val locationPill = createTagPill(locationLabel, hasEndMargin = true)
        val categoryPill = createTagPill(job.category, hasEndMargin = false)

        tagsRow.addView(typePill)
        tagsRow.addView(locationPill)
        tagsRow.addView(categoryPill)

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
            setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3))
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
