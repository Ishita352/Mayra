package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraWhatsAppImportantChannelTest {
    private val message = MayraWhatsAppImportantChannel.Message(
        MayraWhatsAppImportantChannel.Category.INCOME,
        MayraWhatsAppImportantChannel.Priority.HIGH,
        "Income opportunity",
        "A verified opportunity is available.",
        "https://example.com/source"
    )

    @Test fun integrationAndOwnerApprovalAreRequired() {
        assertEquals(
            MayraWhatsAppImportantChannel.Delivery.PREPARE_ONLY,
            MayraWhatsAppImportantChannel.deliveryDecision(message, false, false)
        )
        assertEquals(
            MayraWhatsAppImportantChannel.Delivery.OWNER_APPROVAL_REQUIRED,
            MayraWhatsAppImportantChannel.deliveryDecision(message, true, false)
        )
    }

    @Test fun authorizedDeliveryCanBeSent() {
        assertEquals(
            MayraWhatsAppImportantChannel.Delivery.SEND_VIA_AUTHORIZED_INTEGRATION,
            MayraWhatsAppImportantChannel.deliveryDecision(message, true, true)
        )
    }

    @Test fun invalidSourceIsBlocked() {
        val invalid = message.copy(sourceUrl = "http://unsafe.example")
        assertEquals(
            MayraWhatsAppImportantChannel.Delivery.BLOCKED,
            MayraWhatsAppImportantChannel.deliveryDecision(invalid, true, true)
        )
    }

    @Test fun secretsAndFinancialActionsAreProtected() {
        assertTrue(MayraWhatsAppImportantChannel.privacyRule().contains("OTP"))
        assertTrue(MayraWhatsAppImportantChannel.noFinancialActionRule().contains("bank transfer"))
    }
}
