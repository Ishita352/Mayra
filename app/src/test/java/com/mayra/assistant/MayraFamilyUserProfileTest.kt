package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraFamilyUserProfileTest {
    @Test fun onlyOwnerCanManageProfiles() {
        assertTrue(MayraFamilyUserProfilePolicy.canCreateOrEditProfiles(MayraUserIdentity.Role.OWNER))
        assertFalse(MayraFamilyUserProfilePolicy.canCreateOrEditProfiles(MayraUserIdentity.Role.FAMILY_USER))
        assertTrue(MayraFamilyUserProfilePolicy.canChangePermissions(MayraUserIdentity.Role.OWNER))
        assertFalse(MayraFamilyUserProfilePolicy.canChangePermissions(MayraUserIdentity.Role.FAMILY_USER))
    }

    @Test fun profilePermissionsAreIndividuallyControlled() {
        val profile = MayraFamilyUserProfile(
            userId = "family-01",
            displayName = "Family Member",
            permissions = setOf(
                MayraFamilyAccessPolicy.Capability.CAMERA
            )
        )
        assertTrue(MayraFamilyUserProfilePolicy.mayUse(profile, MayraFamilyAccessPolicy.Capability.CAMERA))
        assertFalse(MayraFamilyUserProfilePolicy.mayUse(profile, MayraFamilyAccessPolicy.Capability.MEDIA))
    }

    @Test fun ownerOnlyCapabilitiesCannotBeGrantedToFamilyUser() {
        val permissions = MayraFamilyUserProfilePolicy.sanitizePermissions(
            setOf(
                MayraFamilyAccessPolicy.Capability.CAMERA,
                MayraFamilyAccessPolicy.Capability.OWNER_MEMORY,
                MayraFamilyAccessPolicy.Capability.USER_MANAGEMENT
            )
        )
        assertTrue(MayraFamilyAccessPolicy.Capability.CAMERA in permissions)
        assertFalse(MayraFamilyAccessPolicy.Capability.OWNER_MEMORY in permissions)
        assertFalse(MayraFamilyAccessPolicy.Capability.USER_MANAGEMENT in permissions)
    }
}
