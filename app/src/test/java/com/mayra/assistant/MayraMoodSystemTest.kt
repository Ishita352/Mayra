package com.mayra.assistant

import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Test

class MayraMoodSystemTest {
    @Test fun allRequestedMoodsExist() {
        assertTrue(MayraMoodSystem.all().size >= 10)
        assertNotNull(MayraMoodSystem.Mood.FUN)
        assertNotNull(MayraMoodSystem.Mood.FRIENDLY)
        assertNotNull(MayraMoodSystem.Mood.CALM)
    }

    @Test fun voiceCommandsSelectMoods() {
        assertEquals(MayraMoodSystem.Mood.FUN, MayraMoodSystem.commandMood("মায়রা ফান মোড চালু করো"))
        assertEquals(MayraMoodSystem.Mood.PROFESSIONAL, MayraMoodSystem.commandMood("Mayra professional mood"))
        assertEquals(MayraMoodSystem.Mood.TEACHER, MayraMoodSystem.commandMood("মায়রা টিচার মোড"))
    }

    @Test fun moodDoesNotChangeSecurityPolicy() {
        assertTrue(MayraMoodSystem.rule().contains("permissions"))
        assertTrue(MayraMoodSystem.rule().contains("financial"))
    }
}
