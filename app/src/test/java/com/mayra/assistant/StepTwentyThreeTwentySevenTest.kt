package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class StepTwentyThreeTwentySevenTest {
 @Test fun ownerSkillsNeedEvidence(){assertEquals(1,MayraOwnerSkillMastery.verifiedSkills(listOf(MayraOwnerSkillMastery.Skill("Kotlin",true,"project"))).size)}
 @Test fun androidReleaseNeedsAllChecks(){assertTrue(MayraAndroidReleaseVerification.releaseReady(MayraAndroidReleaseVerification.baselineChecks()))}
 @Test fun windowsPairingRequiresOwnerAndBootstrap(){assertTrue(MayraWindowsSecurePairing.approve(MayraWindowsSecurePairing.PairingRequest("win10",true,true)));assertFalse(MayraWindowsSecurePairing.approve(MayraWindowsSecurePairing.PairingRequest("win10",false,true)))}
 @Test fun windowsControlBlocksFinancial(){assertFalse(MayraWindowsControlExpansion.allowed(MayraWindowsControlExpansion.Action.FINANCIAL_TRANSACTION))}
 @Test fun windowsSecurityBaselinePasses(){assertTrue(MayraWindowsSecurityTestPolicy.passed())}
}