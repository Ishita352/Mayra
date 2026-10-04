package com.mayra.assistant

object MayraFamilyAccessPolicy {
    enum class Capability {
        BASIC_PHONE_USE,
        VOICE_ASSISTANT,
        CAMERA,
        MEDIA,
        APPROVED_APPS,
        FILES,
        WHATSAPP_ASSISTANCE,
        PC_CONTROL,
        SECURITY_SETTINGS,
        OWNER_MEMORY,
        OWNER_ACCOUNT,
        USER_MANAGEMENT
    }

    fun defaultFamilyCapabilities(): Set<Capability> =
        setOf(
            Capability.BASIC_PHONE_USE,
            Capability.VOICE_ASSISTANT,
            Capability.CAMERA,
            Capability.MEDIA
        )

    fun ownerOnlyCapabilities(): Set<Capability> =
        setOf(
            Capability.OWNER_MEMORY,
            Capability.OWNER_ACCOUNT,
            Capability.USER_MANAGEMENT
        )

    fun mayUse(capability: Capability, grantedByOwner: Boolean): Boolean =
        grantedByOwner && capability !in ownerOnlyCapabilities()

    fun mayAccessOwnerData(capability: Capability): Boolean =
        capability !in ownerOnlyCapabilities()

    fun ownerCanGrant(capability: Capability): Boolean =
        capability !in ownerOnlyCapabilities()

    fun familyUsersMayUseOwnerPhone(): Boolean = true

    fun ownerCanRevokeAnyFamilyAccess(): Boolean = true
}
