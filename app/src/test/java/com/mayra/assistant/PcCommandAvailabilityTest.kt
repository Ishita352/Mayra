package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class PcCommandAvailabilityTest {
    @Test
    fun coreWindowsCommandsAreReady() {
        assertTrue(PcCommandAvailability.currentlyImplemented(PcCommandCatalog.CommandId.PING))
        assertTrue(PcCommandAvailability.currentlyImplemented(PcCommandCatalog.CommandId.GET_PC_STATUS))
        assertTrue(PcCommandAvailability.currentlyImplemented(PcCommandCatalog.CommandId.OPEN_NOTEPAD))
    }

    @Test
    fun publishedButUnimplementedCommandsStayBlockedUntilModuleReady() {
        assertTrue(PcCommandAvailability.requiresCompanionModule(PcCommandCatalog.CommandId.SCREEN_VIEW))
        assertTrue(PcCommandAvailability.requiresCompanionModule(PcCommandCatalog.CommandId.BROWSER_AUTOMATION))
        assertTrue(PcCommandAvailability.requiresCompanionModule(PcCommandCatalog.CommandId.PHONE_CAMERA_FRONT))
    }
}
