package com.mayra.assistant

/**
 * Consent-based local device connection contract for Mayra.
 *
 * Wi-Fi/hotspot connectivity alone is never treated as authorization.
 * A target device must explicitly pair and grant a scoped capability set.
 */
object NetworkDeviceControlPolicy {
    enum class Transport { WIFI_LAN, HOTSPOT_LAN }
    enum class PairingState { UNPAIRED, PAIRING, PAIRED, REVOKED, EXPIRED }
    enum class Capability {
        DEVICE_INFO, OPEN_SHARED_FILE, SEND_FILE, RECEIVE_FILE, CLIPBOARD_SYNC,
        NOTIFICATION_MIRROR, MEDIA_CONTROL, SCREEN_VIEW, SCREEN_CONTROL, APP_LAUNCH,
        PHONE_CAMERA_FRONT, PHONE_CAMERA_BACK, PHONE_MICROPHONE, PHONE_SPEAKER,
        PHONE_REMOTE_RECOVERY
    }

    fun supportedTransports(): Set<Transport> = setOf(Transport.WIFI_LAN, Transport.HOTSPOT_LAN)
    fun mayTreatNetworkPresenceAsConsent(): Boolean = false
    fun requiresExplicitPairing(): Boolean = true
    fun requiresOwnerApprovalForSensitiveCapability(capability: Capability): Boolean =
        capability in setOf(Capability.SCREEN_VIEW, Capability.SCREEN_CONTROL,
            Capability.APP_LAUNCH, Capability.CLIPBOARD_SYNC, Capability.NOTIFICATION_MIRROR)
    fun mayControlDevice(state: PairingState, capabilityGranted: Boolean): Boolean =
        state == PairingState.PAIRED && capabilityGranted
    fun mayAccessWithoutPairing(): Boolean = false
    fun mayBypassLockScreen(): Boolean = false
    fun mayBypassPasswordOrBiometrics(): Boolean = false
    fun mayInstallSoftwareWithoutApproval(): Boolean = false
    fun mayExecuteArbitraryCommands(): Boolean = false
    fun mayPersistAfterRevocation(): Boolean = false
}
