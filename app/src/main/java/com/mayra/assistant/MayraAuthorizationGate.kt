package com.mayra.assistant

/**
 * Central authorization decision layer used before sensitive Mayra actions.
 *
 * It combines session identity, family capability, feature toggles, device
 * lock state and the permanent financial prohibition. It never grants access
 * by itself; callers must provide the relevant verified state.
 */
object MayraAuthorizationGate {
    enum class Action {
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
        USER_MANAGEMENT,
        FINANCIAL_TRANSACTION
    }

    data class Request(
        val identity: MayraSessionIdentityPolicy.SessionIdentity,
        val capability: MayraFamilyAccessPolicy.Capability? = null,
        val ownerVerified: Boolean = false,
        val deviceLocked: Boolean = false,
        val featureEnabled: Boolean = true,
        val masterEnabled: Boolean = true
    )

    fun mayExecute(action: Action, request: Request): Boolean {
        if (!request.masterEnabled || request.deviceLocked || !request.featureEnabled) return false
        if (action == Action.FINANCIAL_TRANSACTION) return false

        if (request.identity == MayraSessionIdentityPolicy.SessionIdentity.OWNER) {
            return request.ownerVerified || action == Action.BASIC_PHONE_USE
        }

        val capability = request.capability ?: return false
        val mapped = capabilityFor(action)
        if (mapped != capability) return false
        if (capability in MayraFamilyAccessPolicy.ownerOnlyCapabilities()) return false
        return MayraFamilyAccessPolicy.mayUse(capability, grantedByOwner = true)
    }

    private fun capabilityFor(action: Action): MayraFamilyAccessPolicy.Capability? =
        when (action) {
            Action.BASIC_PHONE_USE -> MayraFamilyAccessPolicy.Capability.BASIC_PHONE_USE
            Action.VOICE_ASSISTANT -> MayraFamilyAccessPolicy.Capability.VOICE_ASSISTANT
            Action.CAMERA -> MayraFamilyAccessPolicy.Capability.CAMERA
            Action.MEDIA -> MayraFamilyAccessPolicy.Capability.MEDIA
            Action.APPROVED_APPS -> MayraFamilyAccessPolicy.Capability.APPROVED_APPS
            Action.FILES -> MayraFamilyAccessPolicy.Capability.FILES
            Action.WHATSAPP_ASSISTANCE -> MayraFamilyAccessPolicy.Capability.WHATSAPP_ASSISTANCE
            Action.PC_CONTROL -> MayraFamilyAccessPolicy.Capability.PC_CONTROL
            Action.SECURITY_SETTINGS -> MayraFamilyAccessPolicy.Capability.SECURITY_SETTINGS
            Action.OWNER_MEMORY -> MayraFamilyAccessPolicy.Capability.OWNER_MEMORY
            Action.OWNER_ACCOUNT -> MayraFamilyAccessPolicy.Capability.OWNER_ACCOUNT
            Action.USER_MANAGEMENT -> MayraFamilyAccessPolicy.Capability.USER_MANAGEMENT
            Action.FINANCIAL_TRANSACTION -> null
        }
}
