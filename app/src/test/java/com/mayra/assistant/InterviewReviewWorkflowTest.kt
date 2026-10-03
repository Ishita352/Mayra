package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class InterviewReviewWorkflowTest {
    @Test fun completedPracticeIsReady() {
        assertEquals(InterviewReviewWorkflow.Result.READY,
            InterviewReviewWorkflow.review("Excel interview", true, true).result)
    }
    @Test fun prohibitedAiUseForcesHumanOnly() {
        assertEquals(InterviewReviewWorkflow.Result.HUMAN_ONLY,
            InterviewReviewWorkflow.review("Interview", true, false).result)
    }
    @Test fun liveScreenReviewIsDenied() {
        assertFalse(InterviewReviewWorkflow.mayReviewLiveInterviewScreen())
        assertFalse(InterviewReviewWorkflow.mayCovertlyAssist())
    }
}