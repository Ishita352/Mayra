package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraFamilyAccessPolicyTest {
    @Test fun familyCanUseOwnerPhoneWhenOwnerGrantsAccess() {
        assertTrue(MayraFamilyAccessPolicy.familyUsersMayUseOwnerPhone())
        assertTrue(MayraFamilyAccessPolicy.mayUse(
            MayraFamilyAccessPolicy.Capability.CAMERA, true
        ))
        assertFalse(MayraFamilyAccessPolicy.mayUse(
            MayraFamilyAccessPolicy.Capability.CAMERA, false
        ))
    }

    @Test fun ownerDataAndManagementRemainOwnerOnly() {
        assertFalse(MayraFamilyAccessPolicy.mayUse(
            MayraFamilyAccessPolicy.Capability.OWNER_MEMORY, true
        ))
        assertFalse(MayraFamilyAccessPolicy.mayUse(
            MayraFamilyAccessPolicy.Capability.USER_MANAGEMENT, true
        ))
        assertTrue(MayraFamilyAccessPolicy.ownerCanRevokeAnyFamilyAccess())
    }
}
