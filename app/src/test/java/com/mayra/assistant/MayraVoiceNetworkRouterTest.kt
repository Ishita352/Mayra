package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Test

class MayraVoiceNetworkRouterTest {
    @Test
    fun offlinePolicyKeepsLocalTasksLocal() {
        assertEquals(
            MayraVoiceNetworkRouter.Route.LOCAL_OFFLINE_TASK,
            if (MayraOfflineVoiceWorkflow.canWorkOffline(MayraOfflineVoiceWorkflow.Task.DOCUMENT_EDIT))
                MayraVoiceNetworkRouter.Route.LOCAL_OFFLINE_TASK
            else
                MayraVoiceNetworkRouter.Route.QUEUE_FOR_LATER
        )
    }

    @Test
    fun cloudUploadNeverBecomesAnOfflineUpload() {
        assertEquals(
            MayraOfflineVoiceWorkflow.Decision.QUEUE_FOR_LATER,
            MayraOfflineVoiceWorkflow.decide(
                MayraOfflineVoiceWorkflow.Task.CLOUD_UPLOAD,
                online = false
            )
        )
    }

    @Test
    fun cloudUploadRequiresOwnerApprovalWhenOnline() {
        assertEquals(
            MayraOfflineVoiceWorkflow.Decision.OWNER_APPROVAL_REQUIRED,
            MayraOfflineVoiceWorkflow.decide(
                MayraOfflineVoiceWorkflow.Task.CLOUD_UPLOAD,
                online = true,
                ownerApproved = false
            )
        )
    }
}
