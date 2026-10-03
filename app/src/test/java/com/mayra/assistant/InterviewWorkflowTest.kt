package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterviewWorkflowTest {
    @Test
    fun plansCvBasedPreparation() {
        val p = InterviewWorkflow.plan("আমার CV দেখে interview প্রশ্ন practice করাও")
        assertTrue(p.recognized)
        assertTrue(InterviewWorkflow.Action.REVIEW_CV in p.actions)
        assertTrue(InterviewWorkflow.Action.PRACTICE_QUESTION in p.actions)
    }

    @Test
    fun plansMeaningAnswerAndPronunciation() {
        val p = InterviewWorkflow.plan("English প্রশ্নের বাংলা meaning ও answer pronunciation দাও")
        assertTrue(InterviewWorkflow.Action.EXPLAIN_QUESTION in p.actions)
        assertTrue(InterviewWorkflow.Action.PREPARE_ANSWER in p.actions)
        assertTrue(InterviewWorkflow.Action.PRONUNCIATION in p.actions)
    }

    @Test
    fun covertAssistanceAndRuleBypassAreDenied() {
        assertFalse(InterviewWorkflow.mayCovertlyAssist())
        assertFalse(InterviewWorkflow.mayBypassInterviewRules())
    }
}


    @Test fun aiAssistanceIsBlockedWhenBanned() {
        assertFalse(InterviewWorkflow.mayUseAi(InterviewWorkflow.AiPermission.BANNED))
        assertTrue(InterviewWorkflow.mayUseAi(InterviewWorkflow.AiPermission.ALLOWED))
        assertTrue(InterviewWorkflow.mayUseAi(InterviewWorkflow.AiPermission.NOT_PROHIBITED))
    }
