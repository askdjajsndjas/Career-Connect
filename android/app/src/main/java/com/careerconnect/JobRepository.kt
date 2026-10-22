package com.careerconnect

object JobRepository {

    private val sampleJobs = mutableListOf(
        JobListing(
            id = "job_1",
            title = "Software Development Intern",
            company = "ABC Technologies",
            companyLogoRes = R.drawable.ic_company_abc,
            location = "Pretoria, South Africa",
            employmentType = "Full-time",
            isRemote = true,
            category = "IT & Tech",
            description = "We are seeking a motivated Software Development Intern to join our engineering team. You will work on real-world Kotlin & Java applications, contribute to code reviews, and gain hands-on software design experience.",
            requirements = listOf(
                "Currently pursuing a degree in Computer Science, IT or related field",
                "Basic knowledge of Kotlin, Java, or Python",
                "Strong problem-solving and algorithmic skills",
                "Good communication and team collaboration abilities"
            ),
            postDate = "2d ago",
            applicationUrl = "https://example.com/careers/abc-tech-intern"
        ),
        JobListing(
            id = "job_2",
            title = "Marketing Intern",
            company = "Bright Minds Media",
            companyLogoRes = R.drawable.ic_company_bmm,
            location = "Sandton, Johannesburg",
            employmentType = "Internship",
            isRemote = false,
            category = "Marketing",
            description = "Join our digital marketing team to assist with campaign management, social media strategy, content creation, and analytics tracking for leading South African brands.",
            requirements = listOf(
                "Studying Marketing, Communications, or Media Studies",
                "Experience with social media platforms and content creation tools",
                "Strong written and verbal English communication skills",
                "Creative mindset with attention to detail"
            ),
            postDate = "3d ago",
            applicationUrl = "https://example.com/careers/bmm-marketing-intern"
        ),
        JobListing(
            id = "job_3",
            title = "Data Analyst Intern",
            company = "DataWise Solutions",
            companyLogoRes = R.drawable.ic_company_abc,
            location = "Pretoria, South Africa",
            employmentType = "Internship",
            isRemote = true,
            category = "IT & Tech",
            description = "DataWise Solutions is looking for a Data Analyst Intern to extract, clean, and visualize data trends. You will work with SQL, Python, and BI tools to deliver actionable business insights.",
            requirements = listOf(
                "Background in Information Systems, Statistics, or Applied Mathematics",
                "Familiarity with SQL, Excel, and Python data libraries (Pandas/NumPy)",
                "Analytical mindset and curiosity for pattern recognition"
            ),
            postDate = "2d ago",
            applicationUrl = "https://example.com/careers/datawise-analyst"
        ),
        JobListing(
            id = "job_4",
            title = "Business Analyst Trainee",
            company = "InnovateX",
            companyLogoRes = R.drawable.ic_company_bmm,
            location = "Johannesburg, South Africa",
            employmentType = "Full-time",
            isRemote = false,
            category = "Business",
            description = "InnovateX is hiring a Business Analyst Trainee to support project management, gather stakeholder requirements, and assist with process documentation.",
            requirements = listOf(
                "Degree in Business Administration, Commerce, or Industrial Engineering",
                "Proficiency in Microsoft Office / Google Workspace suite",
                "Excellent organizational and presentation skills"
            ),
            postDate = "5d ago",
            applicationUrl = "https://example.com/careers/innovatex-ba"
        ),
        JobListing(
            id = "job_5",
            title = "IT Support Technician",
            company = "TechConnect",
            companyLogoRes = R.drawable.ic_company_abc,
            location = "Durban, South Africa",
            employmentType = "Part-time",
            isRemote = false,
            category = "IT & Tech",
            description = "Provide technical support for hardware, software, and networking infrastructure. Excellent entry-level opportunity for students seeking part-time IT experience.",
            requirements = listOf(
                "Diploma or Degree in Information Technology or Network Systems",
                "Understanding of OS configuration, hardware troubleshooting, and TCP/IP networks",
                "Customer-service oriented attitude"
            ),
            postDate = "1w ago",
            applicationUrl = "https://example.com/careers/techconnect-support"
        )
    )

    fun getAllJobs(): List<JobListing> = sampleJobs

    fun getJobById(id: String): JobListing? = sampleJobs.find { it.id == id }

    fun filterJobs(query: String = "", category: String = "All", type: String = "All"): List<JobListing> {
        return sampleJobs.filter { job ->
            val matchesQuery = query.isEmpty() || 
                job.title.contains(query, ignoreCase = true) ||
                job.company.contains(query, ignoreCase = true) ||
                job.description.contains(query, ignoreCase = true) ||
                job.location.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || job.category.equals(category, ignoreCase = true)

            val matchesType = when (type) {
                "All" -> true
                "Remote" -> job.isRemote
                else -> job.employmentType.equals(type, ignoreCase = true)
            }

            matchesQuery && matchesCategory && matchesType
        }
    }

    fun getTrackedJobs(stage: String = "All"): List<JobListing> {
        val tracked = sampleJobs.filter { it.isSaved || it.applicationStatus != "Not Applied" }
        return when (stage) {
            "All" -> tracked
            "Saved" -> tracked.filter {
                it.isSaved || it.applicationStatus.equals("Saved", ignoreCase = true)
            }
            "Applied" -> tracked.filter {
                it.applicationStatus.equals(ApplicationRepository.PENDING, ignoreCase = true) ||
                    it.applicationStatus.equals("Applied", ignoreCase = true) ||
                    it.applicationStatus.equals("Application Started", ignoreCase = true)
            }
            "Interview" -> tracked.filter {
                it.applicationStatus.equals(ApplicationRepository.INTERVIEW, ignoreCase = true) ||
                    it.applicationStatus.equals("Interview", ignoreCase = true)
            }
            else -> tracked.filter { it.applicationStatus.equals(stage, ignoreCase = true) }
        }
    }

    fun toggleSaved(jobId: String): Boolean {
        val job = getJobById(jobId)
        if (job != null) {
            job.isSaved = !job.isSaved
            if (job.isSaved && job.applicationStatus == "Not Applied") {
                job.applicationStatus = "Saved"
            } else if (!job.isSaved && job.applicationStatus == "Saved") {
                job.applicationStatus = "Not Applied"
            }
            return job.isSaved
        }
        return false
    }

    fun updateStatus(jobId: String, status: String) {
        val job = getJobById(jobId)
        if (job != null) {
            job.applicationStatus = status
            if (status == "Saved") {
                job.isSaved = true
            } else if (status == "Not Applied") {
                job.isSaved = false
            }
        }
    }
}
