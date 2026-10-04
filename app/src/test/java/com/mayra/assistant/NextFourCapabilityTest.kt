package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class NextFourCapabilityTest {
 @Test fun universalIncomeRules(){assertEquals(MayraUniversalIncomeRules.Decision.MANUAL_ONLY,MayraUniversalIncomeRules.decide(false,true));assertEquals(MayraUniversalIncomeRules.Decision.AUTOMATE,MayraUniversalIncomeRules.decide(true,true));assertEquals(MayraUniversalIncomeRules.Decision.BLOCK,MayraUniversalIncomeRules.decide(true,false))}
 @Test fun factCheckShowsUncertainty(){assertTrue(MayraAdvancedFactCheck.evaluate(emptyList()).uncertainty)}
 @Test fun sharingIsTimeLimited(){assertTrue(MayraOwnerSharing.valid(MayraOwnerSharing.Grant("family-01","FILES",2000),1000));assertFalse(MayraOwnerSharing.valid(MayraOwnerSharing.Grant("","FILES",2000),1000));assertTrue(MayraOwnerSharing.ownerDataIsolated())}
 @Test fun auditExcludesSecrets(){val r=MayraPrivacyAudit.Record(MayraPrivacyAudit.Event.LOGIN,"owner",1,"login");assertTrue(MayraPrivacyAudit.valid(r));assertTrue(MayraPrivacyAudit.secretSafe("permission changed"));assertFalse(MayraPrivacyAudit.secretSafe("password=secret"))}
}