package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraUserIdentityTest {
    @Test fun supportsOneOwnerAndTenFamilyUsers() {
        assertEquals(11, MayraUserIdentity.maxUserSlots())
        assertEquals(10, MayraUserIdentity.familyUserIds().size)
    }

    @Test fun familyIdsAreStable() {
        assertTrue(MayraUserIdentity.isValidFamilyUserId("family-01"))
        assertTrue(MayraUserIdentity.isValidFamilyUserId("family-10"))
        assertFalse(MayraUserIdentity.isValidFamilyUserId("family-11"))
    }

    @Test fun onlyOwnerHasFullControl() {
        assertTrue(MayraUserIdentity.hasFullControl(MayraUserIdentity.Role.OWNER))
        assertFalse(MayraUserIdentity.hasFullControl(MayraUserIdentity.Role.FAMILY_USER))
        assertTrue(MayraUserIdentity.mayManageUsers(MayraUserIdentity.Role.OWNER))
        assertFalse(MayraUserIdentity.mayManageUsers(MayraUserIdentity.Role.FAMILY_USER))
    }
}
