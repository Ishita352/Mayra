package com.mayra.assistant

/**
 * Timing policy for Mayra's Incoming Call Assistant.
 *
 * Android does not expose a universal "carrier will disconnect this ringing
 * call at exactly X milliseconds" value. Therefore the assistant uses an
 * estimated ring window and answers two seconds before that window ends,
 * cancelling immediately if the call stops ringing.
 */
object MayraIncomingCallPolicy {
    const val ANSWER_BEFORE_RING_END_MS = 2_000L
    const val DEFAULT_ESTIMATED_RING_DURATION_MS = 30_000L

    fun answerDelayMs(
        estimatedRingDurationMs: Long = DEFAULT_ESTIMATED_RING_DURATION_MS
    ): Long =
        (estimatedRingDurationMs - ANSWER_BEFORE_RING_END_MS).coerceAtLeast(0L)

    fun mayAutoAnswer(
        masterOn: Boolean,
        assistantEnabled: Boolean,
        securityEnabled: Boolean
    ): Boolean =
        masterOn && assistantEnabled && securityEnabled

    fun shouldAnswerAt(
        nowMs: Long,
        ringingSinceMs: Long,
        estimatedRingDurationMs: Long = DEFAULT_ESTIMATED_RING_DURATION_MS
    ): Boolean =
        nowMs - ringingSinceMs >= answerDelayMs(estimatedRingDurationMs)

    fun cancelsWhenCallStopsRinging(): Boolean = true
}
