package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraMoodSystemTest {
    @Test fun allRequestedMoodsExist() {
        assertEquals(10, MayraMoodSystem.all().size)
        assertNotNull(MayraMoodSystem.Mood.NOTTY_GIRL)
        assertNotNull(MayraMoodSystem.Mood.PROFESSIONAL)
    }

    @Test fun voiceCommandsSelectRequestedMoods() {
        assertEquals(MayraMoodSystem.Mood.FUN, MayraMoodSystem.commandMood("মায়রা ফান মোড চালু করো"))
        assertEquals(MayraMoodSystem.Mood.SWEET, MayraMoodSystem.commandMood("Mayra sweet mood"))
        assertEquals(MayraMoodSystem.Mood.NOTTY_GIRL, MayraMoodSystem.commandMood("নটি গার্ল মোড"))
        assertEquals(MayraMoodSystem.Mood.PROFESSIONAL, MayraMoodSystem.commandMood("Mayra professional mood"))
    }

    @Test fun everyMoodHasDistinctBodyReaction() {
        val reactions = MayraMoodSystem.all().map { it.bodyReaction }.toSet()
        assertEquals(10, reactions.size)
    }

    @Test fun moodDoesNotChangeSecurityPolicy() {
        assertTrue(MayraMoodSystem.rule().contains("permissions"))
        assertTrue(MayraMoodSystem.rule().contains("financial"))
    }
}
