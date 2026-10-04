package com.mayra.assistant

object MayraSessionIdentityPolicy {
    enum class SessionIdentity {
        OWNER,
        FAMILY_USER
    }

    fun identityAfterDeviceUnlock(ownerVerified: Boolean): SessionIdentity =
        if (ownerVerified) SessionIdentity.OWNER else SessionIdentity.FAMILY_USER

    fun mayUsePhone(identity: SessionIdentity): Boolean = true

    fun mayIssueMayraCommand(identity: SessionIdentity): Boolean =
        identity == SessionIdentity.OWNER

    fun mayAccessOwnerMemory(identity: SessionIdentity): Boolean =
        identity == SessionIdentity.OWNER

    fun mayManageFamilyUsers(identity: SessionIdentity): Boolean =
        identity == SessionIdentity.OWNER

    fun mayChangeMayraSecurity(identity: SessionIdentity): Boolean =
        identity == SessionIdentity.OWNER

    fun mayTreatDeviceUnlockAsOwnerVerification(): Boolean = false
}
