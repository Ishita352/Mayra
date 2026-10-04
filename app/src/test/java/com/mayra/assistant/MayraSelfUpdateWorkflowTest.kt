package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class MayraSelfUpdateWorkflowTest {
 @Test fun ownerApprovedChangeCanBePrepared(){val r=MayraSelfUpdateWorkflow.Request("Improve feature",true);assertTrue(MayraSelfUpdateWorkflow.canEditCode(r));assertEquals(MayraSelfUpdateWorkflow.Stage.READY_TO_APPLY,MayraSelfUpdateWorkflow.nextStage(r,true))}
 @Test fun elevatedChangeNeedsOwnerApproval(){val r=MayraSelfUpdateWorkflow.Request("Change system integration",false,setOf("system_settings"),MayraSelfUpdateWorkflow.Risk.ELEVATED);assertTrue(MayraSelfUpdateWorkflow.canEditCode(r));assertEquals(MayraSelfUpdateWorkflow.Stage.PERMISSION_REQUIRED,MayraSelfUpdateWorkflow.nextStage(r,false))}
 @Test fun financialUpdateIsBlocked(){val r=MayraSelfUpdateWorkflow.Request("Change payment logic",true,setOf("financial_transaction"));assertFalse(MayraSelfUpdateWorkflow.canEditCode(r))}
 @Test fun securityBypassIsBlocked(){assertTrue(MayraSelfUpdateWorkflow.blockedOperations().contains("disable_security_controls"));assertTrue(MayraSelfUpdateWorkflow.allowedOperations().contains("add_tests"))}
}