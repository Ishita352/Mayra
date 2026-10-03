package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FunCyberLabPolicyTest {
    @Test
    fun allSafeCyberModesAreAvailable() {
        assertEquals(7, FunCyberLabPolicy.modes().size)
        assertTrue(FunCyberLabPolicy.modes().contains(FunCyberLabPolicy.Mode.FAKE_HACKER_TERMINAL))
        assertTrue(FunCyberLabPolicy.modes().contains(FunCyberLabPolicy.Mode.CTF_SANDBOX))
    }

    @Test
    fun cyberLabIsSimulationOnly() {
        assertTrue(FunCyberLabPolicy.isSimulationOnly())
        assertTrue(!FunCyberLabPolicy.mayTargetThirdPartyDevice())
        assertTrue(!FunCyberLabPolicy.mayCollectRealCredentials())
        assertTrue(!FunCyberLabPolicy.mayRunRealPasswordCracking())
        assertTrue(!FunCyberLabPolicy.mayScanThirdPartyNetwork())
        assertTrue(!FunCyberLabPolicy.mayBypassSecurityControl())
        assertTrue(!FunCyberLabPolicy.mayDeployMalwareOrSpyware())
        assertTrue(!FunCyberLabPolicy.mayCaptureOtpOrAuthenticationSecrets())
    }
}
