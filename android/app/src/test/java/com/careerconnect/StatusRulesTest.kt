package com.careerconnect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusRulesTest {

    private val day = 24L * 60 * 60 * 1000
    private val now = 1_800_000_000_000L

    @Test
    fun pending_can_move_to_interview_rejected_or_archived() {
        assertTrue(StatusRules.canTransition(StatusRules.PENDING, StatusRules.INTERVIEW))
        assertTrue(StatusRules.canTransition(StatusRules.PENDING, StatusRules.REJECTED))
        assertTrue(StatusRules.canTransition(StatusRules.PENDING, StatusRules.ARCHIVED))
    }

    @Test
    fun archived_is_final() {
        assertFalse(StatusRules.canTransition(StatusRules.ARCHIVED, StatusRules.PENDING))
        assertFalse(StatusRules.canTransition(StatusRules.ARCHIVED, StatusRules.INTERVIEW))
    }

    @Test
    fun rejected_cannot_go_back_to_pending_or_interview() {
        assertFalse(StatusRules.canTransition(StatusRules.REJECTED, StatusRules.PENDING))
        assertFalse(StatusRules.canTransition(StatusRules.REJECTED, StatusRules.INTERVIEW))
    }

    @Test
    fun staying_on_the_same_status_is_not_a_transition() {
        assertFalse(StatusRules.canTransition(StatusRules.PENDING, StatusRules.PENDING))
    }

    @Test
    fun unknown_status_is_invalid_and_has_no_transitions() {
        assertFalse(StatusRules.isValid("HIRED"))
        assertFalse(StatusRules.canTransition("HIRED", StatusRules.ARCHIVED))
        assertTrue(StatusRules.isValid(StatusRules.INTERVIEW))
    }

    @Test
    fun pending_application_older_than_timeout_is_stale() {
        assertTrue(StatusRules.isStale(StatusRules.PENDING, now - 15 * day, now))
    }

    @Test
    fun pending_application_inside_timeout_is_not_stale() {
        assertFalse(StatusRules.isStale(StatusRules.PENDING, now - 13 * day, now))
    }

    @Test
    fun exactly_at_the_timeout_is_not_yet_stale() {
        assertFalse(StatusRules.isStale(StatusRules.PENDING, now - 14 * day, now))
    }

    @Test
    fun only_pending_applications_go_stale() {
        assertFalse(StatusRules.isStale(StatusRules.INTERVIEW, now - 60 * day, now))
        assertFalse(StatusRules.isStale(StatusRules.REJECTED, now - 60 * day, now))
    }

    @Test
    fun record_older_than_twelve_months_is_past_retention() {
        assertTrue(StatusRules.isPastRetention(now - 366 * day, now))
    }

    @Test
    fun record_inside_twelve_months_is_kept() {
        assertFalse(StatusRules.isPastRetention(now - 364 * day, now))
    }

    @Test
    fun notification_text_matches_each_status() {
        assertEquals(
            "Good news! You've been invited to interview.",
            StatusRules.notificationText(StatusRules.INTERVIEW)
        )
        assertEquals(
            "Unfortunately your application was not successful.",
            StatusRules.notificationText(StatusRules.REJECTED)
        )
        assertEquals(
            "Archived: the employer did not respond in time.",
            StatusRules.notificationText(StatusRules.ARCHIVED)
        )
    }
}
