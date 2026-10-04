package com.mayra.assistant

data class MayraFamilyUserProfile(
    val userId: String,
    val displayName: String,
    val enabled: Boolean = true,
    val permissions: Set<MayraFamilyAccessPolicy.Capability> =
        MayraFamilyAccessPolicy.defaultFamilyCapabilities()
)

object MayraFamilyUserProfilePolicy {
    fun canCreateOrEditProfiles(role: MayraUserIdentity.Role): Boolean =
        role == MayraUserIdentity.Role.OWNER

    fun canDeleteProfiles(role: MayraUserIdentity.Role): Boolean =
        role == MayraUserIdentity.Role.OWNER

    fun canChangePermissions(role: MayraUserIdentity.Role): Boolean =
        role == MayraUserIdentity.Role.OWNER

    fun canUseProfile(profile: MayraFamilyUserProfile): Boolean =
        profile.enabled

    fun mayUse(
        profile: MayraFamilyUserProfile,
        capability: MayraFamilyAccessPolicy.Capability
    ): Boolean =
        profile.enabled &&
            capability in profile.permissions &&
            MayraFamilyAccessPolicy.mayUse(capability, grantedByOwner = true)

    fun sanitizePermissions(
        requested: Set<MayraFamilyAccessPolicy.Capability>
    ): Set<MayraFamilyAccessPolicy.Capability> =
        requested - MayraFamilyAccessPolicy.ownerOnlyCapabilities()
}
