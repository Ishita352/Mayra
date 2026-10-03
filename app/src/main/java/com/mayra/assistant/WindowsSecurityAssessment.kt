package com.mayra.assistant

/**
 * Structured Windows security-provider assessment.
 *
 * This is intentionally a status contract, not an antivirus engine.
 * A real malware finding must come from a verified scanner result.
 */
data class WindowsSecurityAssessment(
    val providerName: String,
    val antivirusEnabled: Boolean,
    val realTimeProtectionEnabled: Boolean,
    val definitionsUpToDate: Boolean,
    val firewallEnabled: Boolean,
    val osSupported: Boolean,
    val scannedAtMs: Long,
    val findings: List<String> = emptyList()
) {
    init {
        require(providerName.isNotBlank())
        require(scannedAtMs > 0L)
    }

    fun risk(): DevicePreConnectionSecurityPolicy.Risk = when {
        !osSupported -> DevicePreConnectionSecurityPolicy.Risk.UNKNOWN
        !antivirusEnabled || !realTimeProtectionEnabled -> DevicePreConnectionSecurityPolicy.Risk.SUSPICIOUS
        !definitionsUpToDate || !firewallEnabled -> DevicePreConnectionSecurityPolicy.Risk.SUSPICIOUS
        else -> DevicePreConnectionSecurityPolicy.Risk.CLEAN
    }

    fun asPreConnectionScan(): DevicePreConnectionScan =
        DevicePreConnectionScan(
            risk = risk(),
            scannerName = providerName,
            scannedAtMs = scannedAtMs,
            findings = findings
        )
}
