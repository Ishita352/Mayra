package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraHealthMonitorTest {
    @Test fun repairsUnsafeStateCombinations() {
        val prefs = TestSharedPreferences()
        prefs.edit()
            .putBoolean(MayraFeatureCheckManager.KEY_SETUP_COMPLETED, false)
            .putBoolean("master_on", true)
            .putBoolean(FeatureToggleRegistry.VOICE_COMMAND, true)
            .putBoolean("mayra_voice_light_enabled", true)
            .apply()

        val result = MayraHealthMonitor.run(prefs)

        assertFalse(prefs.getBoolean("master_on", true))
        assertFalse(prefs.getBoolean(FeatureToggleRegistry.VOICE_COMMAND, true))
        assertFalse(prefs.getBoolean("mayra_voice_light_enabled", true))
        assertTrue(result.repaired.isNotEmpty())
    }

    @Test fun clearsIncompleteWindowsPairing() {
        val prefs = TestSharedPreferences()
        prefs.edit()
            .putBoolean(MayraFeatureCheckManager.KEY_SETUP_COMPLETED, true)
            .putBoolean("owner_verified", true)
            .putString("windows_paired_device", "windows-10")
            .putString("windows_paired_host", "192.168.1.20")
            .apply()

        val result = MayraHealthMonitor.run(prefs)

        assertNull(prefs.getString("windows_paired_device", null))
        assertNull(prefs.getString("windows_paired_host", null))
        assertTrue(result.repaired.any { it.contains("Windows pairing") })
    }

    @Test fun healthyStateNeedsNoRepair() {
        val prefs = TestSharedPreferences()
        prefs.edit()
            .putBoolean(MayraFeatureCheckManager.KEY_SETUP_COMPLETED, true)
            .putBoolean("owner_verified", true)
            .putBoolean("master_on", true)
            .putBoolean(FeatureToggleRegistry.VOICE_COMMAND, true)
            .putBoolean("mayra_voice_light_enabled", true)
            .putString("windows_paired_device", "windows-10")
            .putString("windows_paired_host", "192.168.1.20")
            .putString("windows_paired_token", "token")
            .apply()

        val result = MayraHealthMonitor.run(prefs)

        assertTrue(result.repaired.isEmpty())
        assertEquals("HEALTHY", prefs.getString("mayra_health_last_status", null))
    }
}
