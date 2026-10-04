package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraSemanticMemoryBridgeTest {
    private class MemoryStore : MayraSemanticMemory.Store {
        private val values = mutableMapOf<String, String>()
        override fun read(key: String): String? = values[key]
        override fun write(key: String, value: String) { values[key] = value }
    }

    @Test
    fun moduleResumeIsWrittenToSemanticMemory() {
        val memory = MayraSemanticMemory(MemoryStore())
        val bridge = MayraSemanticMemoryBridge(memory)

        bridge.saveModuleResume("Documents", "Continue document validation.")

        assertEquals(
            "Documents\nContinue document validation.",
            bridge.resumeSummary()
        )
    }

    @Test
    fun relevantContextUsesSemanticMemory() {
        val memory = MayraSemanticMemory(MemoryStore())
        val bridge = MayraSemanticMemoryBridge(memory)
        memory.remember("career", "Android developer interview preparation", setOf("job"))

        assertTrue(bridge.relevantContext("Android interview").contains("career"))
    }

    @Test
    fun homeResumeIsAvailable() {
        val memory = MayraSemanticMemory(MemoryStore())
        val bridge = MayraSemanticMemoryBridge(memory)

        bridge.saveHomeResume()

        assertTrue(bridge.resumeSummary()?.contains("Mayra Home") == true)
    }
}
