package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraSemanticMemoryTest {
    private class MemoryStore : MayraSemanticMemory.Store {
        private val values = mutableMapOf<String, String>()
        override fun read(key: String): String? = values[key]
        override fun write(key: String, value: String) { values[key] = value }
    }

    @Test
    fun relevantMemoriesAreRankedByTextAndTags() {
        val memory = MayraSemanticMemory(MemoryStore())
        memory.remember("job", "Prepare CV for Android developer interview", setOf("career", "job"))
        memory.remember("travel", "Plan a family trip", setOf("travel"))
        val result = memory.recall("Android job interview", 2)
        assertEquals("job", result.first().id)
    }

    @Test
    fun resumeStatePersistsAndCanBeRecovered() {
        val memory = MayraSemanticMemory(MemoryStore())
        memory.saveResume("mayra-setup", "Continue Mayra setup", "Finish semantic memory integration", 1234L)
        val resume = memory.resume()
        assertEquals("mayra-setup", resume?.taskId)
        assertEquals("Continue Mayra setup", resume?.title)
        assertEquals("Finish semantic memory integration", resume?.context)
        assertEquals(1234L, resume?.timestampMs)
    }

    @Test
    fun blankQueryDoesNotReturnMemories() {
        val memory = MayraSemanticMemory(MemoryStore())
        memory.remember("a", "Android setup", setOf("android"))
        assertTrue(memory.recall("   ").isEmpty())
    }
}
