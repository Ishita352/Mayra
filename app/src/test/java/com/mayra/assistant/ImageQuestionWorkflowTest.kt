package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageQuestionWorkflowTest {
    @Test fun allowedImageQuestionIsReady() {
        val p = ImageQuestionWorkflow.plan(true, InterviewWorkflow.AiPermission.ALLOWED)
        assertEquals(ImageQuestionWorkflow.Status.READY, p.status)
        assertTrue(ImageQuestionWorkflow.mayReadImage(InterviewWorkflow.AiPermission.ALLOWED))
        assertTrue(ImageQuestionWorkflow.mayAnswer(InterviewWorkflow.AiPermission.ALLOWED))
    }

    @Test fun bannedInterviewBlocksImageAnswer() {
        val p = ImageQuestionWorkflow.plan(true, InterviewWorkflow.AiPermission.BANNED)
        assertEquals(ImageQuestionWorkflow.Status.BLOCKED_AI_POLICY, p.status)
        assertFalse(ImageQuestionWorkflow.mayAnswer(InterviewWorkflow.AiPermission.BANNED))
    }

    @Test fun noImageIsInvalid() {
        val p = ImageQuestionWorkflow.plan(false, InterviewWorkflow.AiPermission.ALLOWED)
        assertEquals(ImageQuestionWorkflow.Status.INVALID_IMAGE, p.status)
        assertFalse(ImageQuestionWorkflow.mayCovertlyCapture())
    }
}