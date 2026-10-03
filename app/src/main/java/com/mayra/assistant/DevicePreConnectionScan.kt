package com.mayra.assistant

/**
 * One-shot security assessment contract executed before LAN pairing.
 * This is an adapter boundary, not an antivirus engine by itself.
 */
data class DevicePreConnectionScan(
    val risk: DevicePreConnectionSecurityPolicy.Risk,
    val scannerName: String,
    val scannedAtMs: Long,
    val findings: List<String>
) {
    init {
        require(scannerName.isNotBlank())
        require(scannedAtMs > 0L)
    }

    fun isConnectionEligible(): Boolean =
        DevicePreConnectionSecurityPolicy.mayConnectAfterOwnerApproval(risk)

    fun userMessage(): String = when (risk) {
        DevicePreConnectionSecurityPolicy.Risk.CLEAN ->
            "Security scan completed. No detected blocking risk from the verified scanner."
        DevicePreConnectionSecurityPolicy.Risk.SUSPICIOUS ->
            "Warning: the target device has suspicious findings. Mayra will not connect."
        DevicePreConnectionSecurityPolicy.Risk.MALICIOUS ->
            "Critical warning: malicious risk was detected. Mayra will block the connection."
        DevicePreConnectionSecurityPolicy.Risk.UNKNOWN ->
            "Warning: the target device could not be fully verified. Mayra will not connect automatically."
    }
}
