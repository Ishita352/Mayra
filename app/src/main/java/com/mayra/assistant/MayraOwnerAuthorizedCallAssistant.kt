package com.mayra.assistant

/**
 * Owner-authorized outbound call/message script policy.
 * Mayra identifies itself as an AI assistant, then delivers only the
 * Owner-approved message. It must not impersonate the Owner or invent content.
 */
object MayraOwnerAuthorizedCallAssistant {
    data class Request(
        val recipientLabel: String,
        val ownerMessage: String,
        val ownerVerified: Boolean,
        val recipientConfirmed: Boolean
    )

    enum class Decision { READY, OWNER_VERIFICATION_REQUIRED, RECIPIENT_CONFIRMATION_REQUIRED, MESSAGE_REQUIRED, BLOCKED }

    fun decide(request: Request): Decision {
        if (request.recipientLabel.isBlank()) return Decision.BLOCKED
        if (!request.ownerVerified) return Decision.OWNER_VERIFICATION_REQUIRED
        if (!request.recipientConfirmed) return Decision.RECIPIENT_CONFIRMATION_REQUIRED
        if (request.ownerMessage.isBlank()) return Decision.MESSAGE_REQUIRED
        return Decision.READY
    }

    fun opening(ownerName: String = "Gopal Basak"): String =
        "নমস্কার, আমি মায়রা। আমি $ownerName-এর AI assistant। " +
            "তিনি আমাকে আপনার সঙ্গে একটি কথা জানাতে বলেছেন।"

    fun script(ownerName: String, ownerMessage: String): String =
        opening(ownerName) + " তিনি যে কথাটি জানাতে বলেছেন তা হলো: " + ownerMessage

    fun rules(): String =
        "Mayra must identify itself as an AI assistant, must not impersonate the Owner, " +
            "must use only the Owner-approved message, and must not add claims or instructions. " +
            "Outbound calling remains subject to Android call permission, recipient confirmation, " +
            "Owner authorization, and applicable device/network rules."
}
