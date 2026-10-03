package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockModePolicyTest {
    @Test
    fun lockedPhoneModeIsOffByDefault() {
        val prefs = FakeSharedPreferences()
        assertFalse(LockModePolicy.isEnabled(prefs))
    }

    @Test
    fun lockedPhoneModeCanBeEnabledAndDisabled() {
        val prefs = FakeSharedPreferences()

        LockModePolicy.setEnabled(prefs, true)
        assertTrue(LockModePolicy.isEnabled(prefs))

        LockModePolicy.setEnabled(prefs, false)
        assertFalse(LockModePolicy.isEnabled(prefs))
    }
}
