package com.careerconnect

import java.io.Serializable

data class JobListing(
    val id: String,
    val title: String,
    val company: String,
    val companyLogoRes: Int,
    val location: String,
    val employmentType: String, // e.g. "Full-time", "Part-time", "Internship"
    val isRemote: Boolean,
    val category: String, // e.g. "Software Engineering", "Data Science", "Networking & Cloud", etc.
    val description: String,
    val requirements: List<String>,
    val postDate: String,
    val applicationUrl: String,
    var isSaved: Boolean = false,
    var applicationStatus: String = "Not Applied", // "Saved", "Interested", "Application Started", "Applied", "Interview"
    val pathway: String = "",
    val tags: List<String> = emptyList(),
    val southAfricaEligibility: String = "South Africa Eligible"
) : Serializable
