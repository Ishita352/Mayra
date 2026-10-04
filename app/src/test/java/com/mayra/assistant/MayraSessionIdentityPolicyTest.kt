package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraSessionIdentityPolicyTest {
    @Test fun deviceUnlockWithoutOwnerVerificationIsFamilySession() {
        assertEquals(
            MayraSessionIdentityPolicy.SessionIdentity.FAMILY_USER,
            MayraSessionIdentityPolicy.identityAfterDeviceUnlock(false)
        )
        assertTrue(MayraSessionIdentityPolicy.mayUsePhone(
            MayraSessionIdentityPolicy.SessionIdentity.FAMILY_USER
        ))
        assertFalse(MayraSessionIdentityPolicy.mayIssueMayraCommand(
            MayraSessionIdentityPolicy.SessionIdentity.FAMILY_USER
        ))
    }

    @Test fun ownerVerificationEnablesOwnerSession() {
        assertEquals(
            MayraSessionIdentityPolicy.SessionIdentity.OWNER,
            MayraSessionIdentityPolicy.identityAfterDeviceUnlock(true)
        )
        assertTrue(MayraSessionIdentityPolicy.mayIssueMayraCommand(
            MayraSessionIdentityPolicy.SessionIdentity.OWNER
        ))
    }

    @Test fun devicePatternOrPasswordIsNotOwnerVerification() {
        assertFalse(MayraSessionIdentityPolicy.mayTreatDeviceUnlockAsOwnerVerification())
    }
}
