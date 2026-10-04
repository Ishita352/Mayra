package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraVoiceEngineTest {
    @Test
    fun hasTwentyVoiceProfilesIncludingGopalMatch() {
        assertEquals(21, MayraVoiceEngine.profiles.size)
        assertEquals(10, MayraVoiceEngine.profiles.count { it.gender == "female" })
        assertEquals(10, MayraVoiceEngine.profiles.count { it.gender == "male" })
        assertTrue(MayraVoiceEngine.profiles.single { it.isGopalMatch }.id == "gopal_voice_match")
    }

    @Test
    fun profileIdsAreUnique() {
        val ids = MayraVoiceEngine.profiles.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun profileStylesArePresent() {
        assertTrue(MayraVoiceEngine.profiles.all { it.style.isNotBlank() })
    }
}
