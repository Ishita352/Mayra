package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class StepSevenEightNineTest {
 @Test fun reminderValidationAndNextDue(){val now=1000L;val r=MayraNotificationScheduler.Reminder("1","Study",2000);assertTrue(MayraNotificationScheduler.validate(r));assertEquals(r,MayraNotificationScheduler.nextDue(listOf(r),now))}
 @Test fun onlineTestModesBlockUnsafeActions(){val p=MayraOnlineTestWorkflow.plan("Assessment",MayraOnlineTestWorkflow.Mode.AUTHORIZED_ASSISTANCE);assertTrue(p.allowedActions.contains("explain"));assertTrue(p.blockedActions.any{it.contains("proctoring")})}
 @Test fun textileWorkflowAdvancesAndValidates(){var d=MayraTextileDesignWorkflow.Design("Saree","Floral brief",listOf("red"),listOf("flower"));assertTrue(MayraTextileDesignWorkflow.valid(d));d=MayraTextileDesignWorkflow.advance(d);assertEquals(MayraTextileDesignWorkflow.Stage.CONCEPT,d.stage)}
}