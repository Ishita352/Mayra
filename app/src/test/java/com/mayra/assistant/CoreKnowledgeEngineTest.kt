package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreKnowledgeEngineTest {
    @Test
    fun routesMajorDomainsToStableWorkflows() {
        assertEquals(CoreKnowledgeEngine.Domain.DOCUMENTS, CoreKnowledgeEngine.answer("পিডিএফ বানাতে চাই").domain)
        assertEquals(CoreKnowledgeEngine.Domain.EXCEL, CoreKnowledgeEngine.answer("Excel data analysis").domain)
        assertEquals(CoreKnowledgeEngine.Domain.JOBS, CoreKnowledgeEngine.answer("remote freelance job").domain)
        assertEquals(CoreKnowledgeEngine.Domain.INCOME, CoreKnowledgeEngine.answer("income opportunity").domain)
        assertEquals(CoreKnowledgeEngine.Domain.INTERVIEW, CoreKnowledgeEngine.answer("interview preparation").domain)
        assertEquals(CoreKnowledgeEngine.Domain.TEXTILE, CoreKnowledgeEngine.answer("jacquard design").domain)
        assertEquals(CoreKnowledgeEngine.Domain.SECURITY, CoreKnowledgeEngine.answer("cyber security check").domain)
    }

    @Test
    fun blankRequestIsNotRecognized() {
        val result = CoreKnowledgeEngine.answer("   ")
        assertEquals(CoreKnowledgeEngine.Domain.GENERAL, result.domain)
        assertTrue(!result.recognized)
    }

    @Test
    fun ownerSkillRequestsReachOwnerSkillDomain() {
        val result = CoreKnowledgeEngine.answer("আমার Google Maps exact pin-এর কাজ")
        assertEquals(CoreKnowledgeEngine.Domain.OWNER_SKILLS, result.domain)
        assertTrue(result.recognized)
    }
}
