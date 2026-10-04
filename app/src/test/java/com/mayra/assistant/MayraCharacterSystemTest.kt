package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraCharacterSystemTest {
    @Test fun hasTenCharacterSlots() {
        assertEquals(10, MayraCharacterSystem.all().size)
        assertEquals("character_01_sari", MayraCharacterSystem.all().first().id)
    }

    @Test fun everyCharacterSupportsCoreAnimationLayers() {
        assertTrue(MayraCharacterSystem.all().all { it.supportsLipSync })
        assertTrue(MayraCharacterSystem.all().all { it.supportsBlink })
        assertTrue(MayraCharacterSystem.all().all { it.supportsEyeHeadMotion })
        assertTrue(MayraCharacterSystem.all().all { it.supportsGestures })
        assertTrue(MayraCharacterSystem.all().all { it.supportsWalking })
    }

    @Test fun unknownCharacterCannotBeSelected() {
        val fakePrefs = android.app.Application().getSharedPreferences("test_character", 0)
        assertFalse(MayraCharacterSystem.select(fakePrefs, "unknown"))
    }
}
