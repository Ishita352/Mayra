package com.mayra.assistant

/**
 * Cross-device safety and Owner-authorized control policy for Mayra's Android
 * and Windows 10 devices.
 *
 * "Full control" means the maximum control exposed by each OS and its granted
 * permissions; Mayra must never bypass OS security boundaries.
 */
object MayraDeviceSafetyAndControl {
    enum class Device { ANDROID, WINDOWS_10 }
    enum class Risk { SAFE, CAUTION, HIGH_RISK, BLOCKED }
    enum class Action {
        READ_STATUS, OPEN_APP, FILE_ACCESS, SETTINGS, DISPLAY, SOUND, NETWORK,
        CAMERA, MICROPHONE, NOTIFICATIONS, SYSTEM_ADMIN, INSTALL_OR_UPDATE,
        DELETE_DATA, SECURITY_CONTROL, FINANCIAL_TRANSACTION
    }

    data class DeviceState(
        val device: Device,
        val ownerVerified: Boolean,
        val paired: Boolean,
        val osPermissionGranted: Boolean,
        val storageHealthy: Boolean = true,
        val batteryHealthy: Boolean = true,
        val thermalHealthy: Boolean = true
    )

    data class Decision(
        val allowed: Boolean,
        val risk: Risk,
        val reason: String
    )

    fun assess(state: DeviceState, action: Action): Decision {
        if (!state.ownerVerified) return Decision(false, Risk.BLOCKED, "Owner verification required.")
        if (!state.paired) return Decision(false, Risk.BLOCKED, "Secure device pairing required.")
        if (action == Action.FINANCIAL_TRANSACTION) {
            return Decision(false, Risk.BLOCKED, "Financial transactions are permanently blocked.")
        }
        if (action == Action.SECURITY_CONTROL) {
            return Decision(false, Risk.BLOCKED, "Mayra must not disable or bypass security controls.")
        }
        if (!state.osPermissionGranted) {
            return Decision(false, Risk.BLOCKED, "Required OS permission is not granted.")
        }
        if (!state.storageHealthy || !state.batteryHealthy || !state.thermalHealthy) {
            return Decision(false, Risk.CAUTION, "Device health is not safe for this operation.")
        }
        val risk = when (action) {
            Action.DELETE_DATA, Action.INSTALL_OR_UPDATE, Action.SYSTEM_ADMIN -> Risk.HIGH_RISK
            Action.SETTINGS, Action.NETWORK, Action.CAMERA, Action.MICROPHONE -> Risk.CAUTION
            else -> Risk.SAFE
        }
        return Decision(true, risk, "Allowed within OS permissions and Owner authorization.")
    }

    fun protectionRule() =
        "Before important operations Mayra should check device health, OS permissions, secure pairing and Owner authorization; it should avoid actions that could damage hardware, corrupt data, overheat the device, exhaust storage or disable security."

    fun fullControlRule() =
        "Mayra may access the maximum Android/Windows 10 controls that the Owner explicitly authorizes and the OS legitimately exposes. It must never bypass OS security, elevation, permissions, pairing, or device protections."

    fun ownerPermissionRule() =
        "Privileged, destructive, security-sensitive or system-administration actions require explicit Owner authorization; ordinary read/status operations remain permission-bound."

    fun twoDeviceRule() =
        "The Android Owner device and paired Windows 10 Owner device are treated as protected devices. Cross-device commands require secure pairing plus current Owner authorization."

    fun damagePreventionRule() =
        "No operation should intentionally damage hardware, corrupt/delete data without authorization, disable safety/security controls, or continue a risky operation when device health indicates danger."
}
