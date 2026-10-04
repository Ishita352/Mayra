package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraBackgroundCommandRouterTest {
    @Test fun presentationControlsWorkWithoutActivityUi() {
        val prefs = TestSharedPreferences()
        var result = MayraBackgroundCommandRouter.route(prefs, "voice light on")
        assertTrue(result.handled)
        assertTrue(prefs.getBoolean("mayra_voice_light_enabled", false))

        result = MayraBackgroundCommandRouter.route(prefs, "3d off")
        assertTrue(result.handled)
        assertFalse(prefs.getBoolean("mayra_3d_character_enabled", true))
    }

    @Test fun moodAndCharacterSelectionPersist() {
        val prefs = TestSharedPreferences()
        val mood = MayraBackgroundCommandRouter.route(prefs, "Mayra fun mood")
        assertTrue(mood.handled)
        assertEquals(MayraMoodSystem.Mood.FUN, MayraMoodSystem.current(prefs))

        val character = MayraBackgroundCommandRouter.route(prefs, "Mayra Sari")
        assertTrue(character.handled)
        assertEquals("character_01_sari", MayraCharacterSystem.current(prefs).id)
    }

    @Test fun masterOffIsSafeAndStopsFutureBackgroundExecution() {
        val prefs = TestSharedPreferences()
        prefs.edit().putBoolean("master_on", true).apply()
        val result = MayraBackgroundCommandRouter.route(prefs, "Mayra off")
        assertTrue(result.handled)
        assertFalse(prefs.getBoolean("master_on", true))
    }
}
