package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraMessagingPolicyTest {
    @Test fun notificationAccessIsRequired() {
        assertFalse(MayraMessagingPolicy.mayReadIncomingMessages(false))
        assertTrue(MayraMessagingPolicy.mayReadIncomingMessages(true))
    }

    @Test fun replyRequiresAccessAndOwnerApproval() {
        assertFalse(MayraMessagingPolicy.maySendReply(false, true))
        assertFalse(MayraMessagingPolicy.maySendReply(true, false))
        assertTrue(MayraMessagingPolicy.maySendReply(true, true))
    }

    @Test fun privateIdentityAndLocationAreBlocked() {
        assertTrue(MayraMessagingPolicy.publicLookupOnly())
        assertFalse(MayraMessagingPolicy.mayExposePrivateAddress())
        assertFalse(MayraMessagingPolicy.mayExposePrivatePhoneOwnerIdentity())
        assertFalse(MayraMessagingPolicy.mayExposeLiveLocation())
        assertFalse(MayraMessagingPolicy.mayBypassTelegramPrivacy())
        assertFalse(MayraMessagingPolicy.mayRevealHiddenAdminIdentity())
    }
}
