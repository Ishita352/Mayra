package com.mayra.assistant

object MayraOwnerRecoveryChallenge {
    private const val EXPECTED_ANSWER = "গোপাল বসাক"

    fun question(): String =
        "মায়রাকে কে বানিয়েছে?"

    fun verifyAnswer(answer: String): Boolean =
        normalize(answer) == normalize(EXPECTED_ANSWER)

    fun mayOfferAfterPrimaryVerificationFailure(): Boolean = true

    private fun normalize(value: String): String =
        value.trim().replace(Regex("\\s+"), " ")
}
