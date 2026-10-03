package com.mayra.assistant

object MayraMessagingPolicy {
    fun mayReadIncomingMessages(notificationAccessEnabled: Boolean): Boolean =
        notificationAccessEnabled

    fun maySendReply(notificationAccessEnabled: Boolean, ownerApproved: Boolean): Boolean =
        notificationAccessEnabled && ownerApproved

    fun publicLookupOnly(): Boolean = true
    fun mayExposePrivateAddress(): Boolean = false
    fun mayExposePrivatePhoneOwnerIdentity(): Boolean = false
    fun mayExposeLiveLocation(): Boolean = false
    fun mayBypassTelegramPrivacy(): Boolean = false
    fun mayRevealHiddenAdminIdentity(): Boolean = false
}
