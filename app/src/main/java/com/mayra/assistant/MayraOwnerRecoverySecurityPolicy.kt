package com.mayra.assistant

object MayraOwnerRecoverySecurityPolicy {
    enum class RecoveryResult {
        ANSWER_REJECTED,
        VOICE_VERIFICATION_REQUIRED,
        OWNER_VERIFIED,
        MAYRA_SESSION_LOCKED
    }

    fun afterRecoveryAnswerCorrect(): RecoveryResult =
        RecoveryResult.VOICE_VERIFICATION_REQUIRED

    fun verifyOwnerVoice(voiceMatchesOwner: Boolean): RecoveryResult =
        if (voiceMatchesOwner) {
            RecoveryResult.OWNER_VERIFIED
        } else {
            RecoveryResult.MAYRA_SESSION_LOCKED
        }

    fun mayUseMayraAfterRecovery(
        recoveryAnswerCorrect: Boolean,
        ownerVoiceMatches: Boolean
    ): Boolean = recoveryAnswerCorrect && ownerVoiceMatches

    fun mayBypassVoiceAfterRecovery(): Boolean = false

    // This policy locks Mayra's privileged session; it does not silently
    // take over Android's device lock mechanism.
    fun locksMayraSessionOnVoiceMismatch(): Boolean = true
}
