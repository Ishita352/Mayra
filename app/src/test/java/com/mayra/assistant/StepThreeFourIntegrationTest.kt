package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class StepThreeFourIntegrationTest {
    private class Store : MayraWindowsPairingSession.Store {
        private val values = mutableMapOf<String, String>()
        override fun get(key: String): String? = values[key]
        override fun put(key: String, value: String) { values[key] = value }
        override fun remove(key: String) { values.remove(key) }
    }

    @Test fun commandResponseDelayIsExactlyFiveSeconds() {
        assertEquals(5_000L, MayraCommandTimingPolicy.RESPONSE_DELAY_MS)
    }

    @Test fun windowsPairingInvitePersistsUntilAccepted() {
        var now = 1_000L
        val store = Store()
        val session = MayraWindowsPairingSession(store) { now }
        session.saveInvite(MayraWindowsPairingSession.Invite("windows-10", "123456", 301_000L))
        assertEquals("123456", session.pendingInvite()?.code)
        session.markPaired("windows-10")
        assertTrue(session.isPaired())
        assertNull(session.pendingInvite())
        now = 400_000L
        assertEquals("windows-10", session.pairedDeviceId())
    }

    @Test fun expiredPairingInviteIsRemoved() {
        var now = 1_000L
        val store = Store()
        val session = MayraWindowsPairingSession(store) { now }
        session.saveInvite(MayraWindowsPairingSession.Invite("windows-10", "654321", 10_000L))
        now = 10_000L
        assertNull(session.pendingInvite())
    }

    @Test fun oneformaIsRecognizedByCoreKnowledge() {
        val answer = CoreKnowledgeEngine.answer("OneForma project খুঁজে দাও")
        assertEquals(CoreKnowledgeEngine.Domain.ONEFORMA, answer.domain)
        assertTrue(answer.recognized)
    }

    @Test fun jobWatcherDefaultRefreshIsTwoHours() {
        assertEquals(2, JobWatcherWorkflow.ScanPolicy(enabled = true).refreshHours)
    }
}
