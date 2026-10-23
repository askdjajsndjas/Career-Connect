package com.careerconnect

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    @Before
    fun resetRepositoryState() {
        JobRepository.getAllJobs().forEach { job ->
            job.isSaved = false
            job.applicationStatus = "Not Applied"
        }
    }

    @Test
    fun filterJobs_byQueryCategoryAndType_returnsExpectedResults() {
        assertEquals(14, JobRepository.filterJobs().size)
        assertTrue(JobRepository.filterJobs(category = "IT & Tech").size >= 5)
        assertEquals(1, JobRepository.filterJobs(category = "Marketing").size)
        assertTrue(JobRepository.filterJobs(type = "Internship").isNotEmpty())
        assertTrue(JobRepository.filterJobs(type = "Remote").isNotEmpty())
        assertEquals(1, JobRepository.filterJobs(query = "DataWise").size)
        assertEquals(1, JobRepository.filterJobs(query = "Durban").size)
    }

    @Test
    fun filterJobs_supportsAllSixCareerPathways() {
        val softwareJobs = JobRepository.filterJobs(category = JobRepository.PATHWAY_SOFTWARE)
        val dataJobs = JobRepository.filterJobs(category = JobRepository.PATHWAY_DATA)
        val networkJobs = JobRepository.filterJobs(category = JobRepository.PATHWAY_NETWORKING)
        val securityJobs = JobRepository.filterJobs(category = JobRepository.PATHWAY_SECURITY)
        val roboticsJobs = JobRepository.filterJobs(category = JobRepository.PATHWAY_ROBOTICS)
        val supportJobs = JobRepository.filterJobs(category = JobRepository.PATHWAY_IT_SUPPORT)

        assertTrue("Software Engineering should return matching positions", softwareJobs.size >= 2)
        assertTrue("Data Science should return matching positions", dataJobs.size >= 2)
        assertTrue("Networking & Cloud should return matching positions", networkJobs.size >= 2)
        assertTrue("Cybersecurity should return matching positions", securityJobs.size >= 2)
        assertTrue("Robotics & Automation should return matching positions", roboticsJobs.size >= 2)
        assertTrue("IT Support should return matching positions", supportJobs.size >= 2)
    }

    @Test
    fun allRepositoryJobs_areSouthAfricaRelevant() {
        val allJobs = JobRepository.getAllJobs()
        allJobs.forEach { job ->
            assertTrue(
                "Job ${job.id} location '${job.location}' should be in South Africa",
                job.location.contains("South Africa", ignoreCase = true) ||
                    job.location.contains("Johannesburg", ignoreCase = true) ||
                    job.location.contains("Pretoria", ignoreCase = true) ||
                    job.location.contains("Durban", ignoreCase = true) ||
                    job.location.contains("Cape Town", ignoreCase = true)
            )
            assertTrue(
                "Job ${job.id} must be marked as South Africa eligible",
                job.southAfricaEligibility.contains("South Africa", ignoreCase = true)
            )
        }
    }

    @Test
    fun remoteJobEligibility_correctlyDistinguishesSAOpenFromCountryRestricted() {
        val worldwideJob = RemoteJob(title = "AI Engineer", location = "Worldwide")
        assertEquals(JobEligibilityStatus.SA_ELIGIBLE, worldwideJob.getEligibility())

        val saSpecificJob = RemoteJob(title = "Backend Dev", location = "South Africa, EMEA")
        assertEquals(JobEligibilityStatus.SA_ELIGIBLE, saSpecificJob.getEligibility())

        val usOnlyJob = RemoteJob(title = "Rails Dev", location = "USA, Canada, USA timezones")
        assertEquals(JobEligibilityStatus.COUNTRY_RESTRICTED, usOnlyJob.getEligibility())

        val unverifiedJob = RemoteJob(title = "Support Lead", location = "EMEA / Multi-regional")
        assertEquals(JobEligibilityStatus.SA_ELIGIBLE, unverifiedJob.getEligibility())
    }

    @Test
    fun toggleSaved_updatesAndResetsSavedStatusCorrectly() {
        val savedFirst = JobRepository.toggleSaved("job_1")
        val job = JobRepository.getJobById("job_1")!!
        assertTrue(savedFirst)
        assertTrue(job.isSaved)
        assertEquals("Saved", job.applicationStatus)

        val savedSecond = JobRepository.toggleSaved("job_1")
        assertFalse(savedSecond)
        assertFalse(job.isSaved)
        assertEquals("Not Applied", job.applicationStatus)
    }

    @Test
    fun toggleSaved_preservesAppliedStatusWhenUnsaved() {
        JobRepository.toggleSaved("job_2")
        JobRepository.updateStatus("job_2", "Application Started")

        val stillSaved = JobRepository.toggleSaved("job_2")
        val job = JobRepository.getJobById("job_2")!!
        assertFalse(stillSaved)
        assertEquals("Application Started", job.applicationStatus)
    }

    @Test
    fun getTrackedJobs_filtersByStageCorrectly() {
        JobRepository.toggleSaved("job_1") // Saved
        JobRepository.updateStatus("job_2", "Application Started") // Applied stage
        JobRepository.updateStatus("job_3", "Interview") // Interview stage

        assertEquals(3, JobRepository.getTrackedJobs("All").size)
        assertEquals(listOf("job_1"), JobRepository.getTrackedJobs("Saved").map { it.id })
        assertEquals(listOf("job_2"), JobRepository.getTrackedJobs("Applied").map { it.id })
        assertEquals(listOf("job_3"), JobRepository.getTrackedJobs("Interview").map { it.id })
    }

    @Test
    fun dashboardMetrics_reflectActualRepositoryCounts() {
        assertEquals(14, JobRepository.getAllJobs().size)
        assertEquals(0, JobRepository.getTrackedJobs("Saved").size)
        assertEquals(0, JobRepository.getTrackedJobs("Applied").size)
        assertEquals(0, JobRepository.getTrackedJobs("Interview").size)

        JobRepository.toggleSaved("job_1")
        JobRepository.toggleSaved("job_2")
        JobRepository.updateStatus("job_3", "Application Started")
        JobRepository.updateStatus("job_4", "Interview")

        assertEquals(2, JobRepository.getTrackedJobs("Saved").size)
        assertEquals(1, JobRepository.getTrackedJobs("Applied").size)
        assertEquals(1, JobRepository.getTrackedJobs("Interview").size)
        assertEquals(4, JobRepository.getTrackedJobs("All").size)
    }

    @Test
    fun jobRepository_updateStatus_updatesStageAndSavedFlag() {
        JobRepository.updateStatus("job_5", "Saved")
        val job5 = JobRepository.getJobById("job_5")!!
        assertTrue(job5.isSaved)
        assertEquals("Saved", job5.applicationStatus)

        JobRepository.updateStatus("job_5", "Not Applied")
        assertFalse(job5.isSaved)
        assertEquals("Not Applied", job5.applicationStatus)
    }

    @Test
    fun resolveFullName_prioritizesEditedProfileNameOverFirebaseDisplayName() {
        val edited = ProfileActivity.resolveFullName(
            savedName = "Thabo Mbeki",
            firebaseDisplayName = "Original Registration Name",
            firebaseEmail = "thabo@uj.ac.za"
        )
        assertEquals("Thabo Mbeki", edited)

        val fallbackToFirebase = ProfileActivity.resolveFullName(
            savedName = "   ",
            firebaseDisplayName = "Naledi Pandor",
            firebaseEmail = "naledi@uj.ac.za"
        )
        assertEquals("Naledi Pandor", fallbackToFirebase)

        val fallbackToEmail = ProfileActivity.resolveFullName(
            savedName = null,
            firebaseDisplayName = null,
            firebaseEmail = "sipho.ndlovu@uj.ac.za"
        )
        assertEquals("sipho.ndlovu", fallbackToEmail)

        val fallbackToDefault = ProfileActivity.resolveFullName(
            savedName = null,
            firebaseDisplayName = null,
            firebaseEmail = null
        )
        assertEquals("Student", fallbackToDefault)
    }

    @Test
    fun resolveFirstName_extractsFirstNameWithFallback() {
        assertEquals(
            "Lerato",
            ProfileActivity.resolveFirstName(
                savedName = "Lerato Khumalo",
                firebaseDisplayName = "Aiden Mentor"
            )
        )
        assertEquals(
            "Aiden",
            ProfileActivity.resolveFirstName(
                savedName = null,
                firebaseDisplayName = "Aiden Mentor"
            )
        )
        assertEquals(
            "test2",
            ProfileActivity.resolveFirstName(
                savedName = null,
                firebaseDisplayName = null,
                firebaseEmail = "test2@example.com"
            )
        )
        assertEquals(
            "Student",
            ProfileActivity.resolveFirstName(
                savedName = null,
                firebaseDisplayName = null,
                firebaseEmail = null
            )
        )
    }

    @Test
    fun formatCvPreview_containsAllProfileAndCvSections() {
        val preview = CVBuilderActivity.formatCvPreview(
            fullName = "Lerato Khumalo",
            email = "lerato@uj.ac.za",
            phone = "+27 82 555 0199",
            education = "University of Johannesburg",
            qualification = "BSc Computer Science",
            skills = "Kotlin, Android, Firebase",
            summary = "Final-year CS student focused on mobile engineering.",
            experience = "Built Career Connect Android application."
        )

        assertTrue(preview.contains("Lerato Khumalo"))
        assertTrue(preview.contains("lerato@uj.ac.za | +27 82 555 0199"))
        assertTrue(preview.contains("BSc Computer Science — University of Johannesburg"))
        assertTrue(preview.contains("PROFESSIONAL SUMMARY"))
        assertTrue(preview.contains("Final-year CS student focused on mobile engineering."))
        assertTrue(preview.contains("KEY SKILLS"))
        assertTrue(preview.contains("Kotlin, Android, Firebase"))
        assertTrue(preview.contains("WORK & PROJECT EXPERIENCE"))
        assertTrue(preview.contains("Built Career Connect Android application."))
    }

    @Test
    fun jobRepository_supportsFirestoreApplicationStatuses() {
        JobRepository.updateStatus("job_1", ApplicationRepository.PENDING)
        JobRepository.updateStatus("job_2", ApplicationRepository.INTERVIEW)

        val applied = JobRepository.getTrackedJobs("Applied")
        val interview = JobRepository.getTrackedJobs("Interview")

        assertTrue(applied.any { it.id == "job_1" })
        assertTrue(interview.any { it.id == "job_2" })
    }
}
