package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class MayraPersonCaseVerificationTest {
 @Test fun publicRecordsAreSeparated(){val p=MayraPersonCaseVerification.PersonIdentity("Test Person");val c=MayraPersonCaseVerification.PublicCase("C1","F1","PS",true,"Court","Pending",null,"https://gov","2026");val r=MayraPersonCaseVerification.assess(p,listOf(c));assertEquals(1,r.verifiedMatches.size);assertTrue(r.certificateStatus.contains("AUTHORIZED"))}
 @Test fun blankIdentityIsInsufficient(){val p=MayraPersonCaseVerification.PersonIdentity("");val r=MayraPersonCaseVerification.assess(p,emptyList());assertEquals("INSUFFICIENT_IDENTITY",r.identityConfidence)}
}