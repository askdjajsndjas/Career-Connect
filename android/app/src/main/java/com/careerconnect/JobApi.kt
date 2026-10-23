package com.careerconnect

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

enum class JobEligibilityStatus {
    SA_ELIGIBLE,             // Explicitly open to South Africa, Africa, or universal worldwide
    INTERNATIONAL_UNVERIFIED, // Multi-regional or unverified country restrictions
    COUNTRY_RESTRICTED       // Explicitly restricted to USA, Canada, UK, Europe, etc. (excludes South Africa)
}

/**
 * Public job listings from Remotive (https://remotive.com), no API key needed.
 *
 * Remotive's terms: credit Remotive as the source and link to the original
 * posting (`url`) wherever a job is shown. Listings are delayed by 24 hours.
 */
data class RemoteJob(
    val id: Long = 0,
    val url: String? = null,
    val title: String? = null,
    @SerializedName("company_name") val companyName: String? = null,
    val category: String? = null,
    @SerializedName("job_type") val jobType: String? = null,
    @SerializedName("candidate_required_location") val location: String? = null,
    @SerializedName("publication_date") val publishedAt: String? = null
) {
    fun getEligibility(): JobEligibilityStatus {
        val loc = location?.trim() ?: return JobEligibilityStatus.INTERNATIONAL_UNVERIFIED
        val lower = loc.lowercase()

        // 1. Explicit South Africa or Universal worldwide remote
        if (lower.contains("south africa") || lower.contains("anywhere") || lower.contains("worldwide") || lower == "global") {
            return JobEligibilityStatus.SA_ELIGIBLE
        }

        // 2. Explicit restrictions that exclude South Africa
        val excludesSA = lower.contains("usa") || lower.contains("us only") ||
            lower.contains("united states") || lower.contains("canada") ||
            lower.contains("north america") || lower.contains("uk only") ||
            lower.contains("united kingdom") || lower.contains("europe only") ||
            lower.contains("germany") || lower.contains("australia") ||
            lower.contains("apac only") || lower.contains("latam only") ||
            (lower.contains("europe") && !lower.contains("africa") && !lower.contains("emea"))

        if (excludesSA && !lower.contains("south africa") && !lower.contains("worldwide")) {
            return JobEligibilityStatus.COUNTRY_RESTRICTED
        }

        // 3. Multi-regional that includes Africa/South Africa, or general remote without country blocks
        if (lower.contains("africa") || lower.contains("emea")) {
            return JobEligibilityStatus.SA_ELIGIBLE
        }

        return JobEligibilityStatus.INTERNATIONAL_UNVERIFIED
    }

    fun getEligibilityLabel(): String {
        return when (getEligibility()) {
            JobEligibilityStatus.SA_ELIGIBLE -> "🇿🇦 Open to South Africa"
            JobEligibilityStatus.INTERNATIONAL_UNVERIFIED -> "🌐 International • Eligibility Unverified"
            JobEligibilityStatus.COUNTRY_RESTRICTED -> "🚫 Restricted: Not SA Eligible"
        }
    }
}

data class RemoteJobsResponse(
    @SerializedName("job-count") val jobCount: Int = 0,
    val jobs: List<RemoteJob> = emptyList()
)

interface RemotiveService {
    @GET("api/remote-jobs")
    fun getJobs(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("limit") limit: Int? = null
    ): Call<RemoteJobsResponse>
}

object JobApi {

    private val service: RemotiveService by lazy {
        Retrofit.Builder()
            .baseUrl("https://remotive.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RemotiveService::class.java)
    }

    /**
     * Loads jobs and hands the result back on the main thread.
     *
     * Prioritises positions open to South African applicants and filters out
     * clearly country-restricted positions (e.g. USA only, Canada only) unless requested.
     */
    fun fetchJobs(
        category: String? = null,
        search: String? = null,
        includeCountryRestricted: Boolean = false,
        limit: Int = 40,
        onResult: (Result<List<RemoteJob>>) -> Unit
    ) {
        service.getJobs(category, search, null).enqueue(object : Callback<RemoteJobsResponse> {
            override fun onResponse(
                call: Call<RemoteJobsResponse>,
                response: Response<RemoteJobsResponse>
            ) {
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    onResult(Result.failure(Exception("Job service error (${response.code()})")))
                    return
                }
                var jobs = body.jobs

                // Filter by category slug if provided (client-side fallback as Remotive API public feed can ignore query)
                if (!category.isNullOrBlank()) {
                    val catSlug = category.trim().lowercase()
                    jobs = jobs.filter { job ->
                        val jobCat = (job.category ?: "").lowercase()
                        val jobTitle = (job.title ?: "").lowercase()
                        when (catSlug) {
                            "software-dev" -> jobCat.contains("software") || jobCat.contains("dev") || jobTitle.contains("developer") || jobTitle.contains("engineer")
                            "data" -> jobCat.contains("data") || jobCat.contains("analytics") || jobTitle.contains("analyst")
                            "devops-sysadmin" -> jobCat.contains("devops") || jobCat.contains("sysadmin") || jobCat.contains("cloud") || jobTitle.contains("engineer")
                            "design" -> jobCat.contains("design") || jobTitle.contains("ui") || jobTitle.contains("ux")
                            "marketing" -> jobCat.contains("marketing") || jobTitle.contains("marketing")
                            else -> jobCat.contains(catSlug) || jobTitle.contains(catSlug)
                        }
                    }
                }

                // Filter by search query if provided
                if (!search.isNullOrBlank()) {
                    val q = search.trim().lowercase()
                    jobs = jobs.filter {
                        (it.title ?: "").lowercase().contains(q) ||
                            (it.companyName ?: "").lowercase().contains(q) ||
                            (it.location ?: "").lowercase().contains(q) ||
                            (it.category ?: "").lowercase().contains(q)
                    }
                }

                // Filter out country-restricted positions unless explicitly requested
                if (!includeCountryRestricted) {
                    jobs = jobs.filter { it.getEligibility() != JobEligibilityStatus.COUNTRY_RESTRICTED }
                }

                // Prioritise verified South Africa / Worldwide eligible jobs first
                jobs = jobs.sortedByDescending {
                    when (it.getEligibility()) {
                        JobEligibilityStatus.SA_ELIGIBLE -> 2
                        JobEligibilityStatus.INTERNATIONAL_UNVERIFIED -> 1
                        JobEligibilityStatus.COUNTRY_RESTRICTED -> 0
                    }
                }

                onResult(Result.success(jobs.take(limit)))
            }

            override fun onFailure(call: Call<RemoteJobsResponse>, t: Throwable) {
                onResult(Result.failure(t))
            }
        })
    }
}
