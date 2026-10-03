package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PcCommandCatalogTest {
    @Test fun everyPublishedPcCommandIsExplicitlyAllowed() {
        PcCommandCatalog.CommandId.values().forEach {
            assertTrue(PcCommandCatalog.isAllowed(it))
        }
    }

    @Test fun onlyPingCanBeUsedWithoutAnAuthenticatedSession() {
        PcCommandCatalog.CommandId.values().forEach {
            if (it == PcCommandCatalog.CommandId.PING) {
                assertFalse(PcCommandCatalog.requiresAuthenticatedSession(it))
            } else {
                assertTrue(PcCommandCatalog.requiresAuthenticatedSession(it))
            }
        }
    }

    @Test fun settingsAndStatusControlsArePublished() {
        assertTrue(PcCommandCatalog.isAllowed(PcCommandCatalog.CommandId.OPEN_WINDOWS_SETTINGS))
        assertTrue(PcCommandCatalog.isAllowed(PcCommandCatalog.CommandId.OPEN_NETWORK_SETTINGS))
        assertTrue(PcCommandCatalog.isAllowed(PcCommandCatalog.CommandId.OPEN_DISPLAY_SETTINGS))
        assertTrue(PcCommandCatalog.isAllowed(PcCommandCatalog.CommandId.OPEN_SOUND_SETTINGS))
        assertTrue(PcCommandCatalog.isAllowed(PcCommandCatalog.CommandId.GET_PC_STATUS))
        assertTrue(PcCommandCatalog.isAllowed(PcCommandCatalog.CommandId.GET_SECURITY_STATUS))
    }
}
