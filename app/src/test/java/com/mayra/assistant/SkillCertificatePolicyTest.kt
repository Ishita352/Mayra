package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillCertificatePolicyTest {
    @Test fun mayraCannotImpersonateOwner() {
        assertFalse(SkillCertificatePolicy.mayraImpersonateOwnerForCertification())
    }

    @Test fun mayraCannotInventExternalCertificate() {
        assertFalse(SkillCertificatePolicy.mayraClaimExternalCertificateWithoutEvidence())
    }

    @Test fun verifiedSkillWithEvidenceCanEnterBiodata() {
        assertTrue(
            SkillCertificatePolicy.mayAddVerifiedSkillToOwnerBiodata(
                skillVerified = true,
                evidenceAvailable = true
            )
        )
    }

    @Test fun unverifiedSkillCannotEnterBiodata() {
        assertFalse(
            SkillCertificatePolicy.mayAddVerifiedSkillToOwnerBiodata(
                skillVerified = false,
                evidenceAvailable = false
            )
        )
    }
}
