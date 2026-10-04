package com.mayra.assistant

import android.content.Context

/**
 * Bridges Mayra's semantic memory into the live Android session flow.
 *
 * Only resumable task context is stored; secrets and authentication material
 * are never written by this bridge.
 */
class MayraSemanticMemoryBridge(private val memory: MayraSemanticMemory) {

    fun saveHomeResume() {
        memory.saveResume(
            taskId = "mayra-home",
            title = "Mayra Home",
            context = "Mayra is ready for the next Owner-controlled task."
        )
    }

    fun saveModuleResume(title: String, details: String) {
        require(title.isNotBlank())
        val safeDetails = details.take(MAX_CONTEXT_LENGTH)
        memory.saveResume(
            taskId = "mayra-module",
            title = title,
            context = safeDetails.ifBlank { "Mayra module is open." }
        )
    }

    fun resumeSummary(): String? {
        val state = memory.resume() ?: return null
        return state.title + "\n" + state.context
    }

    fun relevantContext(query: String, limit: Int = 5): String =
        memory.buildContext(query, limit)

    companion object {
        private const val MAX_CONTEXT_LENGTH = 20_000

        fun from(context: Context): MayraSemanticMemoryBridge =
            MayraSemanticMemoryBridge(
                MayraSemanticMemory(MayraSemanticMemory.SharedPreferencesStore(context))
            )
    }
}
