package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraOfflineVoiceWorkflowTest {
 @Test fun localTasksCanRunOffline(){assertEquals(MayraOfflineVoiceWorkflow.Decision.ALLOW_OFFLINE,MayraOfflineVoiceWorkflow.decide(MayraOfflineVoiceWorkflow.Task.PDF_PROCESSING,false));assertTrue(MayraOfflineVoiceWorkflow.canWorkOffline(MayraOfflineVoiceWorkflow.Task.VIDEO_EDITING))}
 @Test fun cloudUploadIsQueuedWhenOffline(){assertEquals(MayraOfflineVoiceWorkflow.Decision.QUEUE_FOR_LATER,MayraOfflineVoiceWorkflow.decide(MayraOfflineVoiceWorkflow.Task.CLOUD_UPLOAD,false))}
 @Test fun cloudUploadNeedsOwnerApprovalWhenOnline(){assertEquals(MayraOfflineVoiceWorkflow.Decision.OWNER_APPROVAL_REQUIRED,MayraOfflineVoiceWorkflow.decide(MayraOfflineVoiceWorkflow.Task.CLOUD_UPLOAD,true));assertEquals(MayraOfflineVoiceWorkflow.Decision.QUEUE_FOR_LATER,MayraOfflineVoiceWorkflow.decide(MayraOfflineVoiceWorkflow.Task.CLOUD_UPLOAD,true,true))}
 @Test fun voiceCommandsControlOfflineWorkflow(){assertEquals("OFFLINE_MODE",MayraOfflineVoiceWorkflow.voiceCommand("মায়রা অফলাইনে কাজ করো"));assertEquals("QUEUE_UPLOAD",MayraOfflineVoiceWorkflow.voiceCommand("মায়রা পরে আপলোড করো"))}
 @Test fun queuedUploadRequiresConnectivityAndOwnerApproval(){val q=MayraOfflineVoiceWorkflow.queueItem("memory-1",100L)!!;assertEquals(MayraOfflineVoiceWorkflow.Decision.QUEUE_FOR_LATER,MayraOfflineVoiceWorkflow.releaseQueued(q,false,true));assertEquals(MayraOfflineVoiceWorkflow.Decision.OWNER_APPROVAL_REQUIRED,MayraOfflineVoiceWorkflow.releaseQueued(q,true,false));assertEquals(MayraOfflineVoiceWorkflow.Decision.QUEUE_FOR_LATER,MayraOfflineVoiceWorkflow.releaseQueued(q,true,true))}
}