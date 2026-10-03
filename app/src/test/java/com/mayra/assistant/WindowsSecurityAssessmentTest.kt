package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Test

class WindowsSecurityAssessmentTest {
    private fun assessment(
        antivirus: Boolean = true,
        realtime: Boolean = true,
        definitions: Boolean = true,
        firewall: Boolean = true,
        supported: Boolean = true
    ) = WindowsSecurityAssessment(
        providerName = "Microsoft Defender",
        antivirusEnabled = antivirus,
        realTimeProtectionEnabled = realtime,
        definitionsUpToDate = definitions,
        firewallEnabled = firewall,
        osSupported = supported,
        scannedAtMs = 1L
    )

    @Test fun fullyProtectedWindowsIsClean() {
        assertEquals(
            DevicePreConnectionSecurityPolicy.Risk.CLEAN,
            assessment().risk()
        )
    }

    @Test fun disabledProtectionIsSuspicious() {
        assertEquals(
            DevicePreConnectionSecurityPolicy.Risk.SUSPICIOUS,
            assessment(realtime = false).risk()
        )
    }

    @Test fun staleDefinitionsAreSuspicious() {
        assertEquals(
            DevicePreConnectionSecurityPolicy.Risk.SUSPICIOUS,
            assessment(definitions = false).risk()
        )
    }

    @Test fun unsupportedWindowsFailsClosed() {
        assertEquals(
            DevicePreConnectionSecurityPolicy.Risk.UNKNOWN,
            assessment(supported = false).risk()
        )
    }
}
