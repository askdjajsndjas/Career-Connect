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
        assertEquals(5, JobRepository.filterJobs().size)
        assertEquals(3, JobRepository.filterJobs(category = "IT & Tech").size)
        assertEquals(1, JobRepository.filterJobs(category = "Marketing").size)
        assertEquals(2, JobRepository.filterJobs(type = "Internship").size)
        assertEquals(2, JobRepository.filterJobs(type = "Remote").size)
        assertEquals(1, JobRepository.filterJobs(query = "DataWise").size)
        assertEquals(1, JobRepository.filterJobs(query = "Durban").size)
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
        assertEquals("Amina Dlamini", fallbackToDefault)
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
            "Amina",
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
}
