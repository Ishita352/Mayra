package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Test

class MayraOwnerAuthorizedCallAssistantTest {
    @Test fun requiresOwnerVerificationAndRecipientConfirmation() {
        val base = MayraOwnerAuthorizedCallAssistant.Request("Person", "Please call me.", false, true)
        assertEquals(
            MayraOwnerAuthorizedCallAssistant.Decision.OWNER_VERIFICATION_REQUIRED,
            MayraOwnerAuthorizedCallAssistant.decide(base)
        )
        assertEquals(
            MayraOwnerAuthorizedCallAssistant.Decision.RECIPIENT_CONFIRMATION_REQUIRED,
            MayraOwnerAuthorizedCallAssistant.decide(base.copy(ownerVerified = true, recipientConfirmed = false))
        )
    }

    @Test fun readyRequestProducesSelfIdentificationAndExactMessage() {
        val request = MayraOwnerAuthorizedCallAssistant.Request(
            "Person", "আপনার মালিক আপনাকে আজ বিকেল ৫টায় ফোন করতে বলেছেন।", true, true
        )
        assertEquals(MayraOwnerAuthorizedCallAssistant.Decision.READY, MayraOwnerAuthorizedCallAssistant.decide(request))
        val script = MayraOwnerAuthorizedCallAssistant.script("Gopal Basak", request.ownerMessage)
        assert(script.contains("আমি মায়রা"))
        assert(script.contains(request.ownerMessage))
    }
}
