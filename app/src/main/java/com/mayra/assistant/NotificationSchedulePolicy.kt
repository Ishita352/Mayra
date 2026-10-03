package com.mayra.assistant

/**
 * Notification/scheduling policy foundation.
 * Actual Android WorkManager integration will be added in the Android integration phase.
 */
object NotificationSchedulePolicy {
    enum class EventType { JOB_OPPORTUNITY, INCOME_UPDATE, INTERVIEW, LEARNING, SECURITY, GENERAL }

    data class Event(
        val type: EventType,
        val title: String,
        val message: String,
        val important: Boolean = false
    )

    fun mayNotify(event: Event): Boolean =
        event.title.isNotBlank() && event.message.isNotBlank()

    fun requiresOwnerAction(event: Event): Boolean = when (event.type) {
        EventType.JOB_OPPORTUNITY, EventType.INCOME_UPDATE,
        EventType.INTERVIEW, EventType.SECURITY -> true
        EventType.LEARNING, EventType.GENERAL -> event.important
    }

    fun mayRunStealthily(): Boolean = false
    fun mayTriggerFinancialAction(): Boolean = false
}
