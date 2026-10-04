package com.mayra.assistant

/**
 * Owner-requested command timing contract.
 *
 * Mayra waits five seconds after a recognized command is received, then gives
 * its response and starts the requested workflow. The delay is intentionally
 * centralized so every voice command follows the same behavior.
 */
object MayraCommandTimingPolicy {
    const val RESPONSE_DELAY_MS: Long = 5_000L
}
