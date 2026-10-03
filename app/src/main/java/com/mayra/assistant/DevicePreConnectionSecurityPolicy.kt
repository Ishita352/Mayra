package com.mayra.assistant

/**
 * Pre-connection security gate for consent-based device linking.
 *
 * A network connection is never established merely because a device is
 * reachable. Mayra must first receive a security assessment and owner approval.
 * This policy does not claim to be a full antivirus engine; actual malware
 * detection must come from an installed/verified security scanner or a
 * dedicated scan adapter.
 */
object DevicePreConnectionSecurityPolicy {
    enum class Risk {
        CLEAN,
        SUSPICIOUS,
        MALICIOUS,
        UNKNOWN
    }

    enum class GateState {
        SCAN_REQUIRED,
        SCANNING,
        REVIEW_REQUIRED,
        BLOCKED,
        OWNER_APPROVED,
        READY
    }

    fun requiresScanBeforePairing(): Boolean = true

    fun gateFor(risk: Risk): GateState = when (risk) {
        Risk.CLEAN -> GateState.REVIEW_REQUIRED
        Risk.SUSPICIOUS, Risk.MALICIOUS -> GateState.BLOCKED
        Risk.UNKNOWN -> GateState.REVIEW_REQUIRED
    }

    fun mayAskOwnerForApproval(risk: Risk): Boolean =
        risk == Risk.CLEAN || risk == Risk.UNKNOWN

    fun mayConnectWithoutOwnerApproval(risk: Risk): Boolean = false

    fun mayConnectAfterOwnerApproval(risk: Risk): Boolean =
        risk == Risk.CLEAN

    fun mustBlockAutomatically(risk: Risk): Boolean =
        risk == Risk.MALICIOUS

    fun mustWarnOwner(risk: Risk): Boolean =
        risk == Risk.SUSPICIOUS || risk == Risk.MALICIOUS || risk == Risk.UNKNOWN

    fun mayBypassDetectedThreat(): Boolean = false

    fun mayDisableTargetSecurityControls(): Boolean = false

    fun requiresVerifiedScanSource(): Boolean = true
}
