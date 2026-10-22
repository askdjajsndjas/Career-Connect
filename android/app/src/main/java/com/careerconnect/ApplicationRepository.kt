package com.careerconnect

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.concurrent.TimeUnit

data class JobApplication(
    val id: String = "",
    val jobId: String = "",
    val jobTitle: String = "",
    val company: String = "",
    val status: String = "PENDING",
    val appliedAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

object ApplicationRepository {

    const val PENDING = "PENDING"
    const val INTERVIEW = "INTERVIEW"
    const val REJECTED = "REJECTED"
    const val ARCHIVED = "ARCHIVED"

    // Pending applications older than this are archived automatically
    // (the "employer never responded" rule)
    const val TIMEOUT_DAYS = 14L

    private val db get() = FirebaseFirestore.getInstance()
    private val auth get() = FirebaseAuth.getInstance()

    fun applyToJob(
        jobId: String,
        jobTitle: String,
        company: String,
        employerId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "Not signed in")
            return
        }
        val now = Timestamp.now()
        val data = hashMapOf(
            "studentId" to user.uid,
            "studentName" to (user.displayName ?: ""),
            "studentEmail" to (user.email ?: ""),
            "employerId" to employerId,
            "jobId" to jobId,
            "jobTitle" to jobTitle,
            "company" to company,
            "status" to PENDING,
            "appliedAt" to now,
            "updatedAt" to now
        )
        // One document per student and job, so applying twice can't create duplicates
        db.collection("applications").document("${user.uid}_$jobId").set(data)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage) }
    }

    fun listenToMyApplications(
        onChange: (List<JobApplication>) -> Unit,
        onError: (String) -> Unit = {}
    ): ListenerRegistration? {
        val uid = auth.currentUser?.uid ?: return null
        return db.collection("applications")
            .whereEqualTo("studentId", uid)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    onError(e.localizedMessage ?: "Could not load applications")
                    return@addSnapshotListener
                }
                val list = snap?.documents?.map { d ->
                    JobApplication(
                        id = d.id,
                        jobId = d.getString("jobId") ?: "",
                        jobTitle = d.getString("jobTitle") ?: "",
                        company = d.getString("company") ?: "",
                        status = d.getString("status") ?: PENDING,
                        appliedAt = d.getTimestamp("appliedAt"),
                        updatedAt = d.getTimestamp("updatedAt")
                    )
                } ?: emptyList()
                onChange(list)
            }
    }

    // Archives applications that have sat on PENDING past the timeout
    fun archiveStale(apps: List<JobApplication>) {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(TIMEOUT_DAYS)
        for (a in apps) {
            val applied = a.appliedAt?.toDate()?.time ?: continue
            if (a.status == PENDING && applied < cutoff) {
                db.collection("applications").document(a.id)
                    .update(mapOf("status" to ARCHIVED, "updatedAt" to Timestamp.now()))
            }
        }
    }
}