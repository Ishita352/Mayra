package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraVoiceRuntimeTest {
    @Test
    fun detectsPrimaryLanguages() {
        assertEquals(MayraVoiceRuntime.Language.BENGALI, MayraVoiceRuntime.detectLanguage("মায়রা কাজ করো"))
        assertEquals(MayraVoiceRuntime.Language.HINDI, MayraVoiceRuntime.detectLanguage("मायरा काम करो"))
        assertEquals(MayraVoiceRuntime.Language.ENGLISH, MayraVoiceRuntime.detectLanguage("Mayra work offline"))
    }

    @Test
    fun routesOfflineAndQueueCommands() {
        assertEquals(
            MayraVoiceRuntime.Action.OFFLINE_MODE,
            MayraVoiceRuntime.route("মায়রা অফলাইনে কাজ করো").action
        )
        assertEquals(
            MayraVoiceRuntime.Action.RUN_OFFLINE_TASKS,
            MayraVoiceRuntime.route("run offline tasks").action
        )
        assertEquals(
            MayraVoiceRuntime.Action.QUEUE_UPLOAD,
            MayraVoiceRuntime.route("upload later").action
        )
    }

    @Test
    fun routesInterruption() {
        assertEquals(
            MayraVoiceRuntime.Action.STOP_SPEAKING,
            MayraVoiceRuntime.route("মায়রা থামো").action
        )
    }

    @Test
    fun ownerResponseUsesBossStyle() {
        assertTrue(MayraVoiceRuntime.ownerResponse("আমি প্রস্তুত").startsWith("বস,"))
        assertEquals("বস, আমি প্রস্তুত", MayraVoiceRuntime.ownerResponse("বস, আমি প্রস্তুত"))
    }

    @Test
    fun delegatesOfflineDecisionPolicy() {
        assertEquals(
            MayraOfflineVoiceWorkflow.Decision.ALLOW_OFFLINE,
            MayraVoiceRuntime.offlineDecision(
                MayraOfflineVoiceWorkflow.Task.DOCUMENT_EDIT,
                online = false
            )
        )
        assertEquals(
            MayraOfflineVoiceWorkflow.Decision.QUEUE_FOR_LATER,
            MayraVoiceRuntime.offlineDecision(
                MayraOfflineVoiceWorkflow.Task.CLOUD_UPLOAD,
                online = false
            )
        )
    }
}
