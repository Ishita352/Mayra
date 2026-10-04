package com.mayra.assistant

import org.junit.Assert.assertTrue
import org.junit.Test

class MayraContinuousSelfDevelopmentTest {
    @Test fun selfUpdateRequiresOwnerApproval() {
        val request = MayraSelfUpdateWorkflow.Request("Improve Mayra", ownerApproved = false)
        assertTrue(MayraSelfUpdateWorkflow.approvalRequiredForEveryChange())
        assertTrue(MayraSelfUpdateWorkflow.nextStage(request, testsPassed = true) == MayraSelfUpdateWorkflow.Stage.OWNER_APPROVAL_REQUIRED)
    }
    @Test fun financialUpdateOperationIsBlocked() {
        val request = MayraSelfUpdateWorkflow.Request("payment", true, setOf("financial_transaction"))
        assertTrue(!MayraSelfUpdateWorkflow.canEditCode(request))
    }
}