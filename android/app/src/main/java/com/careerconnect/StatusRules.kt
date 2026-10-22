package com.careerconnect

/**
 * The business rules for application status, kept free of Android and Firebase
 * code so they can be unit tested on a plain JVM.
 *
 * The values match the constants in ApplicationRepository.
 */
object StatusRules {

    const val PENDING = "PENDING"
    const val INTERVIEW = "INTERVIEW"
    const val REJECTED = "REJECTED"
    const val ARCHIVED = "ARCHIVED"

    /** A PENDING application with no employer response after this long is archived. */
    const val TIMEOUT_DAYS = 14L

    /** Application records are kept for 12 months after their last update (POPIA data minimisation). */
    const val RETENTION_DAYS = 365L

    private const val DAY_MS = 24L * 60 * 60 * 1000

    private val allowed: Map<String, Set<String>> = mapOf(
        PENDING to setOf(INTERVIEW, REJECTED, ARCHIVED),
        INTERVIEW to setOf(REJECTED, ARCHIVED),
        REJECTED to setOf(ARCHIVED),
        ARCHIVED to emptySet()
    )

    fun isValid(status: String): Boolean = allowed.containsKey(status)

    /** True when moving from [from] to [to] is a legal step. Staying on the same status is not a step. */
    fun canTransition(from: String, to: String): Boolean = allowed[from]?.contains(to) == true

    /** True when a PENDING application has waited longer than [timeoutDays] with no response. */
    fun isStale(
        status: String,
        appliedAtMillis: Long,
        nowMillis: Long,
        timeoutDays: Long = TIMEOUT_DAYS
    ): Boolean = status == PENDING && nowMillis - appliedAtMillis > timeoutDays * DAY_MS

    /** True when a record was last updated longer ago than the retention period. */
    fun isPastRetention(
        updatedAtMillis: Long,
        nowMillis: Long,
        retentionDays: Long = RETENTION_DAYS
    ): Boolean = nowMillis - updatedAtMillis > retentionDays * DAY_MS

    fun notificationText(status: String): String = when (status) {
        INTERVIEW -> "Good news! You've been invited to interview."
        REJECTED -> "Unfortunately your application was not successful."
        ARCHIVED -> "Archived: the employer did not respond in time."
        else -> "Status updated to $status"
    }
}
