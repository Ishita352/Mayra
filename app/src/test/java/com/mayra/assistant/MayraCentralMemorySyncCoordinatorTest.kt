package com.mayra.assistant
import org.junit.Assert.assertEquals
import org.junit.Test
class MayraCentralMemorySyncCoordinatorTest {
 @Test fun importantItemNeedsPerItemApproval(){val i=MayraCentralMemoryVault.Item("m1",MayraCentralMemoryVault.DataClass.CORE_MEMORY,10,MayraCentralMemoryVault.Importance.IMPORTANT);val c=MayraCentralMemorySyncCoordinator.decide(i,MayraCentralMemoryVault.Config(enabled=true,ownerApprovedForProvider=true),true,false);assertEquals(MayraCentralMemoryVault.Decision.OWNER_APPROVAL_REQUIRED,c.vaultDecision)}
 @Test fun approvedOfflineItemWaits(){val i=MayraCentralMemoryVault.Item("m2",MayraCentralMemoryVault.DataClass.CORE_MEMORY,10,MayraCentralMemoryVault.Importance.IMPORTANT);val c=MayraCentralMemorySyncCoordinator.decide(i,MayraCentralMemoryVault.Config(enabled=true,ownerApprovedForProvider=true),false,true);assertEquals(MayraCentralMemoryVault.Decision.ELIGIBLE_FOR_SYNC,c.vaultDecision);assertEquals(MayraOfflineVoiceWorkflow.Decision.QUEUE_FOR_LATER,c.uploadDecision)}
}