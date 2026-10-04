package com.mayra.assistant

/**
 * Owner WhatsApp important-information channel.
 *
 * Mayra can classify important alerts and prepare a message for the Owner's
 * WhatsApp conversation. Actual delivery must use an authorized WhatsApp
 * Business/API integration; credentials are never stored in message content.
 */
object MayraWhatsAppImportantChannel {
    enum class Priority { CRITICAL, HIGH, NORMAL }
    enum class Category { SECURITY, JOB, INCOME, LEGAL, GOVERNMENT, EDUCATION, SYSTEM, SELF_DEVELOPMENT, OTHER }
    enum class Delivery { PREPARE_ONLY, OWNER_APPROVAL_REQUIRED, SEND_VIA_AUTHORIZED_INTEGRATION, BLOCKED }

    data class Message(
        val category: Category,
        val priority: Priority,
        val title: String,
        val body: String,
        val sourceUrl: String? = null
    )

    fun validate(message: Message): Boolean =
        message.title.isNotBlank() &&
            message.body.isNotBlank() &&
            (message.sourceUrl == null || message.sourceUrl.startsWith("https://"))

    fun deliveryDecision(
        message: Message,
        integrationAuthorized: Boolean,
        ownerApproved: Boolean
    ): Delivery {
        if (!validate(message)) return Delivery.BLOCKED
        if (!integrationAuthorized) return Delivery.PREPARE_ONLY
        if (!ownerApproved) return Delivery.OWNER_APPROVAL_REQUIRED
        return Delivery.SEND_VIA_AUTHORIZED_INTEGRATION
    }

    fun chatPurpose(): String =
        "A dedicated Mayra conversation/channel for important Owner alerts, summaries, job/income opportunities, legal/government updates, education alerts, security events and self-development reminders."

    fun privacyRule(): String =
        "Send only the minimum necessary information to the Owner's authorized WhatsApp destination. Never include passwords, OTPs, payment credentials, biometric templates or secret tokens."

    fun noFinancialActionRule(): String =
        "WhatsApp delivery is notification-only; sending a message must never trigger a purchase, subscription, bank transfer, withdrawal or other financial transaction."

    fun sourceRule(): String =
        "Important factual alerts should retain a source URL where available and distinguish verified information from uncertainty."

    fun ownerControlRule(): String =
        "The Owner controls the destination, integration authorization and whether automated delivery is enabled. Mayra must not silently change the destination."
}
