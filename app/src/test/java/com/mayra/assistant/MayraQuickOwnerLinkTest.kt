package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraQuickOwnerLinkTest {
    @Test fun createsAndExpiresOneTimeLink() {
        val prefs = TestSharedPreferences()
        val link = MayraQuickOwnerLink.create("gopal-owner", nowMs = 1000L)
        assertEquals("gopal-owner", link.ownerId)
        assertEquals(8, link.code.length)
        MayraQuickOwnerLink.save(prefs, link)
        assertNotNull(MayraQuickOwnerLink.current(prefs, 1001L))
        assertNull(MayraQuickOwnerLink.current(prefs, link.expiresAtMs))
    }

    @Test fun payloadAndFingerprintAreDeterministic() {
        val link = MayraQuickOwnerLink.Link("owner-a", "12345678", 9999L)
        assertEquals("MAYRA-LINK|v1|owner=owner-a|code=12345678|expires=9999", MayraQuickOwnerLink.payload(link))
        assertEquals(MayraQuickOwnerLink.fingerprint("owner-a", "12345678"),
            MayraQuickOwnerLink.fingerprint("owner-a", "12345678"))
        assertNotEquals(MayraQuickOwnerLink.fingerprint("owner-a", "12345678"),
            MayraQuickOwnerLink.fingerprint("owner-b", "12345678"))
    }
}
