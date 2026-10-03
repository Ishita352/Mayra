package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JamesBillingsKnowledgeTest {
    @Test fun verifiedKnowledgeContainsCoreWorkflow() {
        assertTrue(JamesBillingsKnowledge.topics.any { it.name == "account_linking" && it.verified })
        assertTrue(JamesBillingsKnowledge.topics.any { it.name == "tasks" && it.verified })
        assertTrue(JamesBillingsKnowledge.topics.any { it.name == "cashout" && it.verified })
        assertTrue(JamesBillingsKnowledge.topics.any { it.name == "paypal" && it.verified })
        assertTrue(JamesBillingsKnowledge.topics.any { it.name == "tango" && it.verified })
    }

    @Test fun ownerRemainsInControl() {
        assertFalse(JamesBillingsKnowledge.mayraMayPerformFinancialAction())
        assertFalse(JamesBillingsKnowledge.mayraMaySubmitAsOwner())
        assertTrue(JamesBillingsKnowledge.requiresOwnerApproval())
    }
}