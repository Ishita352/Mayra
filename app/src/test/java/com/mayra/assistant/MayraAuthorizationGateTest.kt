package com.mayra.assistant

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraAuthorizationGateTest {
    private fun owner(verified: Boolean = true) =
        MayraAuthorizationGate.Request(
            identity = MayraSessionIdentityPolicy.SessionIdentity.OWNER,
            ownerVerified = verified
        )

    private fun family(capability: MayraFamilyAccessPolicy.Capability) =
        MayraAuthorizationGate.Request(
            identity = MayraSessionIdentityPolicy.SessionIdentity.FAMILY_USER,
            capability = capability
        )

    @Test fun financialActionsAreAlwaysDenied() {
        assertFalse(
            MayraAuthorizationGate.mayExecute(
                MayraAuthorizationGate.Action.FINANCIAL_TRANSACTION,
                owner()
            )
        )
    }

    @Test fun familyCapabilityMustMatchAction() {
        assertTrue(
            MayraAuthorizationGate.mayExecute(
                MayraAuthorizationGate.Action.CAMERA,
                family(MayraFamilyAccessPolicy.Capability.CAMERA)
            )
        )
        assertFalse(
            MayraAuthorizationGate.mayExecute(
                MayraAuthorizationGate.Action.CAMERA,
                family(MayraFamilyAccessPolicy.Capability.FILES)
            )
        )
    }

    @Test fun ownerOnlyCapabilityCannotBeGrantedToFamily() {
        assertFalse(
            MayraAuthorizationGate.mayExecute(
                MayraAuthorizationGate.Action.OWNER_MEMORY,
                family(MayraFamilyAccessPolicy.Capability.OWNER_MEMORY)
            )
        )
    }

    @Test fun lockedDisabledOrMasterOffDeniesAll() {
        assertFalse(
            MayraAuthorizationGate.mayExecute(
                MayraAuthorizationGate.Action.BASIC_PHONE_USE,
                owner().copy(deviceLocked = true)
            )
        )
        assertFalse(
            MayraAuthorizationGate.mayExecute(
                MayraAuthorizationGate.Action.BASIC_PHONE_USE,
                owner().copy(featureEnabled = false)
            )
        )
        assertFalse(
            MayraAuthorizationGate.mayExecute(
                MayraAuthorizationGate.Action.BASIC_PHONE_USE,
                owner().copy(masterEnabled = false)
            )
        )
    }

    @Test fun unverifiedOwnerCannotUseSensitiveAction() {
        assertFalse(
            MayraAuthorizationGate.mayExecute(
                MayraAuthorizationGate.Action.VOICE_ASSISTANT,
                owner(verified = false)
            )
        )
    }
}
