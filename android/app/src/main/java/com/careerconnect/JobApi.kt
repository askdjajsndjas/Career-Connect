package com.careerconnect

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

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
)

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
     * The query parameters are sent to the server, but the same filtering is
     * repeated here because the public feed does not always honour them.
     */
    fun fetchJobs(
        category: String? = null,
        search: String? = null,
        limit: Int = 30,
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
                if (!search.isNullOrBlank()) {
                    val q = search.trim().lowercase()
                    jobs = jobs.filter {
                        (it.title ?: "").lowercase().contains(q) ||
                            (it.companyName ?: "").lowercase().contains(q)
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
