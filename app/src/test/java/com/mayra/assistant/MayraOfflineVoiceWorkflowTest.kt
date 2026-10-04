package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraOfflineVoiceWorkflowTest {
    @Test fun localTasksCanRunOffline() {
        assertEquals(
            MayraOfflineVoiceWorkflow.Decision.ALLOW_OFFLINE,
            MayraOfflineVoiceWorkflow.decide(
                MayraOfflineVoiceWorkflow.Task.PDF_PROCESSING,
                online = false
            )
        )
        assertTrue(MayraOfflineVoiceWorkflow.canWorkOffline(MayraOfflineVoiceWorkflow.Task.VIDEO_EDITING))
    }

    @Test fun cloudUploadIsQueuedWhenOffline() {
        assertEquals(
            MayraOfflineVoiceWorkflow.Decision.QUEUE_FOR_LATER,
            MayraOfflineVoiceWorkflow.decide(
                MayraOfflineVoiceWorkflow.Task.CLOUD_UPLOAD,
                online = false
            )
        )
    }

    @Test fun cloudUploadNeedsOwnerApprovalWhenOnline() {
        assertEquals(
            MayraOfflineVoiceWorkflow.Decision.OWNER_APPROVAL_REQUIRED,
            MayraOfflineVoiceWorkflow.decide(
                MayraOfflineVoiceWorkflow.Task.CLOUD_UPLOAD,
                online = true
            )
        )
        assertEquals(
            MayraOfflineVoiceWorkflow.Decision.QUEUE_FOR_LATER,
            MayraOfflineVoiceWorkflow.decide(
                MayraOfflineVoiceWorkflow.Task.CLOUD_UPLOAD,
                online = true,
                ownerApproved = true
            )
        )
    }

    @Test fun voiceCommandsControlOfflineWorkflow() {
        assertEquals(
            "OFFLINE_MODE",
            MayraOfflineVoiceWorkflow.voiceCommand("মায়রা অফলাইনে কাজ করো")
        )
        assertEquals(
            "QUEUE_UPLOAD",
            MayraOfflineVoiceWorkflow.voiceCommand("মায়রা পরে আপলোড করো")
        )
    }
}
