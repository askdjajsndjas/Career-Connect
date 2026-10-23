package com.careerconnect

object JobRepository {

    const val PATHWAY_SOFTWARE = "Software Engineering and Software Development"
    const val PATHWAY_DATA = "Data Science and Data Analytics"
    const val PATHWAY_NETWORKING = "Networking and Cloud Computing"
    const val PATHWAY_SECURITY = "Cybersecurity and Information Security"
    const val PATHWAY_ROBOTICS = "Robotics, Embedded Systems and Automation"
    const val PATHWAY_IT_SUPPORT = "IT Support and Technical Operations"
    const val PATHWAY_BUSINESS = "Business and Management"
    const val PATHWAY_MARKETING = "Marketing and Communications"

    private val sampleJobs = mutableListOf(
        JobListing(
            id = "job_1",
            title = "Software Development Intern",
            company = "ABC Technologies",
            companyLogoRes = R.drawable.ic_company_abc,
            location = "Pretoria, South Africa",
            employmentType = "Full-time",
            isRemote = true,
            category = "Software Engineering",
            pathway = PATHWAY_SOFTWARE,
            tags = listOf("Kotlin", "Java", "Python", "Android", "Git"),
            southAfricaEligibility = "South Africa Eligible (Hybrid / Remote)",
            description = "We are seeking a motivated Software Development Intern to join our engineering team in Pretoria. You will work on real-world Kotlin & Java applications, contribute to code reviews, and gain hands-on software design experience.",
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
            pathway = PATHWAY_MARKETING,
            tags = listOf("Social Media", "SEO", "Content Creation", "Digital Campaigns"),
            southAfricaEligibility = "South Africa (On-site)",
            description = "Join our digital marketing team in Sandton to assist with campaign management, social media strategy, content creation, and analytics tracking for leading South African brands.",
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
            category = "Data Science",
            pathway = PATHWAY_DATA,
            tags = listOf("SQL", "Python", "Pandas", "Power BI", "Data Visualisation"),
            southAfricaEligibility = "South Africa Eligible (Remote)",
            description = "DataWise Solutions is looking for a Data Analyst Intern in Pretoria to extract, clean, and visualize data trends. You will work with SQL, Python, and BI tools to deliver actionable business insights.",
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
            pathway = PATHWAY_BUSINESS,
            tags = listOf("Agile", "Requirements Gathering", "UML", "Process Mapping"),
            southAfricaEligibility = "South Africa (On-site)",
            description = "InnovateX is hiring a Business Analyst Trainee in Johannesburg to support project management, gather stakeholder requirements, and assist with process documentation.",
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
            category = "IT Support",
            pathway = PATHWAY_IT_SUPPORT,
            tags = listOf("Hardware Support", "TCP/IP", "Active Directory", "Windows Server", "Helpdesk"),
            southAfricaEligibility = "South Africa (On-site)",
            description = "Provide technical support for hardware, software, and networking infrastructure. Excellent entry-level opportunity for students in Durban seeking part-time IT experience.",
            requirements = listOf(
                "Diploma or Degree in Information Technology or Network Systems",
                "Understanding of OS configuration, hardware troubleshooting, and TCP/IP networks",
                "Customer-service oriented attitude"
            ),
            postDate = "1w ago",
            applicationUrl = "https://example.com/careers/techconnect-support"
        ),
        JobListing(
            id = "job_6",
            title = "Junior Android Developer",
            company = "Vodacom Digital",
            companyLogoRes = R.drawable.ic_company_bmm,
            location = "Midrand, Johannesburg, South Africa",
            employmentType = "Full-time",
            isRemote = false,
            category = "Software Engineering",
            pathway = PATHWAY_SOFTWARE,
            tags = listOf("Android", "Kotlin", "Coroutines", "Jetpack", "REST APIs"),
            southAfricaEligibility = "South Africa (On-site / Hybrid)",
            description = "Join the Vodacom Digital mobile development team in Midrand. You will contribute to modern Android client applications written in Kotlin, collaborate with UX designers, and practice agile software delivery.",
            requirements = listOf(
                "BSc in Computer Science, IT, or equivalent degree",
                "Proficiency in Kotlin and Android Studio",
                "Familiarity with Git version control and RESTful web services",
                "Eagerness to learn mobile architecture best practices"
            ),
            postDate = "1d ago",
            applicationUrl = "https://example.com/careers/vodacom-android-junior"
        ),
        JobListing(
            id = "job_7",
            title = "Graduate BI & Data Analyst",
            company = "Standard Bank Group",
            companyLogoRes = R.drawable.ic_company_abc,
            location = "Rosebank, Johannesburg, South Africa",
            employmentType = "Full-time",
            isRemote = false,
            category = "Data Science",
            pathway = PATHWAY_DATA,
            tags = listOf("Data Science", "SQL", "Tableau", "Python", "Data Modelling"),
            southAfricaEligibility = "South Africa (On-site / Hybrid)",
            description = "Standard Bank Graduate Program opportunity for aspiring data professionals. Build predictive models, develop executive reporting dashboards, and extract intelligence from banking data pipelines.",
            requirements = listOf(
                "Completed or final-year degree in Data Science, Statistics, Mathematics, or IT",
                "Working knowledge of SQL and Python or R",
                "Strong analytical thinking and data storytelling skills"
            ),
            postDate = "3d ago",
            applicationUrl = "https://example.com/careers/standard-bank-data-grad"
        ),
        JobListing(
            id = "job_8",
            title = "Cloud Systems Associate",
            company = "Dimension Data",
            companyLogoRes = R.drawable.ic_company_bmm,
            location = "Bryanston, Johannesburg, South Africa",
            employmentType = "Full-time",
            isRemote = true,
            category = "Networking & Cloud",
            pathway = PATHWAY_NETWORKING,
            tags = listOf("AWS", "Azure", "Cloud Computing", "Linux", "Terraform", "DevOps"),
            southAfricaEligibility = "South Africa Eligible (Hybrid / Remote)",
            description = "Dimension Data is looking for an entry-level Cloud Systems Associate to support enterprise cloud migrations across AWS and Azure infrastructure. Work with cloud architects on provisioning, automation, and monitoring.",
            requirements = listOf(
                "Degree or National Diploma in Information Technology, Computer Systems or Cloud Computing",
                "Foundational understanding of AWS or Microsoft Azure fundamentals",
                "Basic Linux command line and scripting skills"
            ),
            postDate = "4d ago",
            applicationUrl = "https://example.com/careers/dimension-data-cloud"
        ),
        JobListing(
            id = "job_9",
            title = "Junior Network Engineer",
            company = "BCX",
            companyLogoRes = R.drawable.ic_company_abc,
            location = "Centurion, Pretoria, South Africa",
            employmentType = "Full-time",
            isRemote = false,
            category = "Networking & Cloud",
            pathway = PATHWAY_NETWORKING,
            tags = listOf("Cisco", "CCNA", "Routing", "Switching", "TCP/IP", "Firewalls"),
            southAfricaEligibility = "South Africa (On-site)",
            description = "Assist senior network engineers at BCX Centurion with switch configurations, router provisioning, LAN/WAN monitoring, and firewall rule auditing for South African corporate clients.",
            requirements = listOf(
                "BTech or BSc in Computer Networks or Information Technology",
                "CCNA coursework or certified knowledge",
                "Knowledge of subnetting, VLANs, and VPN protocols"
            ),
            postDate = "5d ago",
            applicationUrl = "https://example.com/careers/bcx-network-junior"
        ),
        JobListing(
            id = "job_10",
            title = "Cybersecurity Analyst Trainee",
            company = "Discovery Limited",
            companyLogoRes = R.drawable.ic_company_bmm,
            location = "Sandton, Johannesburg, South Africa",
            employmentType = "Internship",
            isRemote = true,
            category = "Cybersecurity",
            pathway = PATHWAY_SECURITY,
            tags = listOf("Cybersecurity", "SIEM", "SOC", "Vulnerability Scanning", "Incident Response"),
            southAfricaEligibility = "South Africa Eligible (Hybrid)",
            description = "Discovery's Information Security team in Sandton invites graduate applicants to join our 24/7 Security Operations Center (SOC). Detect anomalies, triage security alerts, and protect vital healthcare platforms.",
            requirements = listOf(
                "Degree or Advanced Diploma in Cybersecurity, Computer Science, or Information Security",
                "Familiarity with common vulnerabilities (OWASP Top 10) and security protocols",
                "High ethical standards and analytical mindset"
            ),
            postDate = "1d ago",
            applicationUrl = "https://example.com/careers/discovery-cyber-trainee"
        ),
        JobListing(
            id = "job_11",
            title = "Information Security Intern",
            company = "First National Bank (FNB)",
            companyLogoRes = R.drawable.ic_company_abc,
            location = "Johannesburg, South Africa",
            employmentType = "Full-time",
            isRemote = false,
            category = "Cybersecurity",
            pathway = PATHWAY_SECURITY,
            tags = listOf("Identity Management", "Cryptography", "POPIA Data Protection", "Infosec"),
            southAfricaEligibility = "South Africa (On-site)",
            description = "FNB is hiring an Information Security Intern in Johannesburg. You will assist the compliance team with POPIA data privacy safeguards, cryptographic key reviews, and internal phishing awareness simulations.",
            requirements = listOf(
                "Final-year student or graduate in Information Technology / Cyber Law / Computer Science",
                "Knowledge of South African data protection principles (POPIA) and encryption concepts",
                "Detail-oriented with strong documentation abilities"
            ),
            postDate = "2d ago",
            applicationUrl = "https://example.com/careers/fnb-infosec-intern"
        ),
        JobListing(
            id = "job_12",
            title = "Robotics & Automation Intern",
            company = "CSIR South Africa",
            companyLogoRes = R.drawable.ic_company_abc,
            location = "Pretoria, South Africa",
            employmentType = "Internship",
            isRemote = false,
            category = "Robotics & Automation",
            pathway = PATHWAY_ROBOTICS,
            tags = listOf("ROS", "Embedded C++", "Microcontrollers", "Robotics", "Automation", "Sensors"),
            southAfricaEligibility = "South Africa (On-site)",
            description = "The Council for Scientific and Industrial Research (CSIR) in Pretoria offers an internship in autonomous systems. Work on robot operating system (ROS) nodes, sensor integration, and industrial automation prototypes.",
            requirements = listOf(
                "Degree in Mechatronics, Electrical & Computer Engineering, or Robotics",
                "Hands-on experience with C++, Python, and Arduino/Raspberry Pi/STM32",
                "Passion for hardware-software integration"
            ),
            postDate = "3d ago",
            applicationUrl = "https://example.com/careers/csir-robotics-intern"
        ),
        JobListing(
            id = "job_13",
            title = "Junior Embedded Firmware Engineer",
            company = "Denel Dynamics",
            companyLogoRes = R.drawable.ic_company_bmm,
            location = "Centurion, South Africa",
            employmentType = "Full-time",
            isRemote = false,
            category = "Robotics & Automation",
            pathway = PATHWAY_ROBOTICS,
            tags = listOf("Embedded Systems", "ARM Cortex", "C/C++", "RTOS", "Hardware Integration"),
            southAfricaEligibility = "South Africa (On-site)",
            description = "Exciting junior engineering role at Denel Dynamics in Centurion. Develop bare-metal firmware and RTOS modules for high-reliability embedded control systems and automated telemetry.",
            requirements = listOf(
                "BEng / BSc in Electronic or Computer Engineering",
                "Proficiency in Embedded C/C++ programming for ARM microcontrollers",
                "Understanding of digital communication buses (I2C, SPI, CAN)"
            ),
            postDate = "4d ago",
            applicationUrl = "https://example.com/careers/denel-embedded-junior"
        ),
        JobListing(
            id = "job_14",
            title = "Junior Systems Administrator",
            company = "Takealot Group",
            companyLogoRes = R.drawable.ic_company_bmm,
            location = "Cape Town, South Africa",
            employmentType = "Full-time",
            isRemote = true,
            category = "IT Support",
            pathway = PATHWAY_IT_SUPPORT,
            tags = listOf("Linux", "Active Directory", "Bash", "Technical Operations", "SysAdmin"),
            southAfricaEligibility = "South Africa Eligible (Hybrid / Remote)",
            description = "Takealot is hiring a Junior Systems Administrator in Cape Town. Support internal technical operations, manage Linux application servers, automate maintenance with Bash, and maintain user access.",
            requirements = listOf(
                "Diploma or Degree in Information Systems or Systems Administration",
                "Solid experience with Ubuntu/Debian/CentOS Linux environments",
                "Problem-solving orientation and curiosity for operations tooling"
            ),
            postDate = "1d ago",
            applicationUrl = "https://example.com/careers/takealot-sysadmin-junior"
        )
    )

    fun getAllJobs(): List<JobListing> = sampleJobs

    fun getJobById(id: String): JobListing? = sampleJobs.find { it.id == id }

fun matchesCategory(job: JobListing, categoryQuery: String): Boolean {
        if (categoryQuery == "All" || categoryQuery.isBlank()) return true
        val cat = categoryQuery.trim()

        if (job.category.equals(cat, ignoreCase = true) || job.pathway.equals(cat, ignoreCase = true)) return true
        if (job.category.contains(cat, ignoreCase = true) || job.pathway.contains(cat, ignoreCase = true)) return true

        val lowerCat = cat.lowercase()

        // Legacy category mappings
        if (lowerCat == "it & tech" || lowerCat == "it") {
            return job.pathway in listOf(
                PATHWAY_SOFTWARE,
                PATHWAY_DATA,
                PATHWAY_NETWORKING,
                PATHWAY_SECURITY,
                PATHWAY_ROBOTICS,
                PATHWAY_IT_SUPPORT
            )
        }
        if (lowerCat == "engineering") {
            return job.pathway == PATHWAY_SOFTWARE || job.pathway == PATHWAY_ROBOTICS ||
                job.title.contains("engineer", ignoreCase = true) ||
                job.category.contains("engineering", ignoreCase = true)
        }

        val titleLower = job.title.lowercase()
        val tagsLower = job.tags.map { it.lowercase() }

        val isSoftware = lowerCat.contains("software") || lowerCat.contains("dev")
        val isData = lowerCat.contains("data") || lowerCat.contains("analytics")
        val isNetwork = lowerCat.contains("network") || lowerCat.contains("cloud") || lowerCat.contains("devops")
        val isSecurity = lowerCat.contains("security") || lowerCat.contains("cyber")
        val isRobotics = lowerCat.contains("robot") || lowerCat.contains("embedded") || lowerCat.contains("automation")
        val isSupport = lowerCat.contains("support") || lowerCat.contains("operation") || lowerCat.contains("technician")
        val isBusiness = lowerCat.contains("business")
        val isMarketing = lowerCat.contains("market")

        if (isSoftware && (job.pathway == PATHWAY_SOFTWARE || titleLower.contains("software") || titleLower.contains("developer") || tagsLower.contains("kotlin"))) return true
        if (isData && (job.pathway == PATHWAY_DATA || titleLower.contains("data") || titleLower.contains("analyst") && !isBusiness || tagsLower.contains("data science"))) return true
        if (isNetwork && (job.pathway == PATHWAY_NETWORKING || titleLower.contains("cloud") || titleLower.contains("network") || tagsLower.contains("cloud computing"))) return true
        if (isSecurity && (job.pathway == PATHWAY_SECURITY || titleLower.contains("security") || titleLower.contains("cyber") || tagsLower.contains("cybersecurity"))) return true
        if (isRobotics && (job.pathway == PATHWAY_ROBOTICS || titleLower.contains("robot") || titleLower.contains("embedded") || titleLower.contains("firmware") || titleLower.contains("automation"))) return true
        if (isSupport && (job.pathway == PATHWAY_IT_SUPPORT || titleLower.contains("support") || titleLower.contains("sysadmin") || titleLower.contains("helpdesk") || titleLower.contains("technician"))) return true
        if (isBusiness && (job.pathway == PATHWAY_BUSINESS || titleLower.contains("business") || tagsLower.contains("business analysis"))) return true
        if (isMarketing && (job.pathway == PATHWAY_MARKETING || titleLower.contains("marketing") || tagsLower.contains("digital marketing"))) return true

        return false
    }

    fun filterJobs(
        query: String = "",
        category: String = "All",
        type: String = "All",
        location: String = "All"
    ): List<JobListing> {
        return sampleJobs.filter { job ->
            val q = query.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                job.title.lowercase().contains(q) ||
                job.company.lowercase().contains(q) ||
                job.description.lowercase().contains(q) ||
                job.location.lowercase().contains(q) ||
                job.pathway.lowercase().contains(q) ||
                job.tags.any { it.lowercase().contains(q) } ||
                job.requirements.any { it.lowercase().contains(q) }

            val matchesCategory = matchesCategory(job, category)

            val matchesType = when (type) {
                "All" -> true
                "Remote" -> job.isRemote
                else -> job.employmentType.equals(type, ignoreCase = true)
            }

            val matchesLocation = when (location) {
                "All" -> true
                "Remote" -> job.isRemote
                else -> job.location.contains(location, ignoreCase = true)
            }

            matchesQuery && matchesCategory && matchesType && matchesLocation
        }
    }

    fun getPersonalizedRecommendations(
        qualification: String?,
        skills: String?,
        careerInterest: String? = null
    ): List<JobListing> {
        val inferredPathway = when {
            !careerInterest.isNullOrBlank() -> careerInterest
            qualification?.contains("data", ignoreCase = true) == true -> PATHWAY_DATA
            qualification?.contains("cyber", ignoreCase = true) == true || qualification?.contains("security", ignoreCase = true) == true -> PATHWAY_SECURITY
            qualification?.contains("network", ignoreCase = true) == true || qualification?.contains("cloud", ignoreCase = true) == true -> PATHWAY_NETWORKING
            qualification?.contains("robot", ignoreCase = true) == true || qualification?.contains("mechatron", ignoreCase = true) == true -> PATHWAY_ROBOTICS
            qualification?.contains("support", ignoreCase = true) == true -> PATHWAY_IT_SUPPORT
            qualification?.contains("market", ignoreCase = true) == true -> PATHWAY_MARKETING
            qualification?.contains("business", ignoreCase = true) == true -> PATHWAY_BUSINESS
            else -> PATHWAY_SOFTWARE
        }

        return sampleJobs.sortedByDescending { job ->
            if (job.pathway == inferredPathway) 2 else if (matchesCategory(job, inferredPathway)) 1 else 0
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
