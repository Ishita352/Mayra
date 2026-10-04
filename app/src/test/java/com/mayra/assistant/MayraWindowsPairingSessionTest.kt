package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraWindowsPairingSessionTest {
    private class Store : MayraWindowsPairingSession.Store {
        private val map = mutableMapOf<String, String>()
        override fun get(key: String) = map[key]
        override fun put(key: String, value: String) { map[key] = value }
        override fun remove(key: String) { map.remove(key) }
    }

    @Test fun pairingInviteExpiresAndPairedStatePersists() {
        val store = Store()
        var now = 1000L
        val session = MayraWindowsPairingSession(store) { now }
        session.saveInvite(MayraWindowsPairingSession.Invite("windows-10", "123456", 2000L))
        assertEquals("123456", session.pendingInvite()?.code)
        now = 2001L
        assertNull(session.pendingInvite())

        session.markPaired("windows-10", "192.168.1.20", 8765, "persistent-token")
        assertTrue(session.isPaired())
        assertEquals("windows-10", session.pairedDeviceId())
        assertEquals("192.168.1.20", session.pairedHost())
        assertEquals(8765, session.pairedPort())
        assertEquals("persistent-token", session.sessionToken())

        // A new session object over the same persistent store represents a reboot/restart.
        val restored = MayraWindowsPairingSession(store) { now }
        assertTrue(restored.isPaired())
        assertEquals("persistent-token", restored.sessionToken())

        restored.revoke()
        assertFalse(restored.isPaired())
        assertNull(restored.sessionToken())
    }
}
