package com.mayra.assistant

/** Privacy-by-design protections for Mayra. */
object MayraPrivacyProtection {
    enum class DataType { LOCATION, DEVICE_ID, USAGE_ANALYTICS, VOICE, CAMERA, CONTACTS, FILES, NETWORK_METADATA }
    enum class Decision { ALLOW_WITH_CONSENT, DENY_BY_DEFAULT, MINIMIZE, OWNER_ONLY }

    fun decision(type: DataType): Decision = when (type) {
        DataType.LOCATION, DataType.VOICE, DataType.CAMERA, DataType.CONTACTS, DataType.FILES -> Decision.DENY_BY_DEFAULT
        DataType.DEVICE_ID, DataType.USAGE_ANALYTICS, DataType.NETWORK_METADATA -> Decision.MINIMIZE
    }

    fun telemetryEnabledByDefault(): Boolean = false
    fun thirdPartyTrackingEnabledByDefault(): Boolean = false
    fun rawBiometricStorageAllowed(): Boolean = false
    fun secretStorageAllowed(): Boolean = false

    /** Mayra must not provide covert anti-forensics or enforcement-evasion behavior. */
    fun covertEvasionAllowed(): Boolean = false

    /** Remote access is visible, authenticated and Owner-approved only. */
    fun remoteAccessRequiresOwnerApproval(): Boolean = true

    /** Sensitive collection requires explicit consent and the minimum necessary scope. */
    fun sensitiveAccessRequiresConsent(): Boolean = true

    /** Persistent identifiers should not be created merely for tracking. */
    fun persistentTrackingIdentifiersAllowed(): Boolean = false

    /** Data retention should be minimized; unnecessary telemetry should not be retained. */
    fun unnecessaryTelemetryRetentionAllowed(): Boolean = false

    fun networkRule(): String =
        "Do not expose Mayra services directly to the public internet by default; prefer authenticated, encrypted, least-privilege connections and explicit Owner approval for remote access."

    fun privacyRule(): String =
        "Collect the minimum data necessary, keep sensitive data local where practical, avoid persistent tracking identifiers, " +
        "do not store raw biometric data or secrets, do not enable analytics or third-party tracking by default, " +
        "and require explicit consent before sensitive sensors or personal data are accessed."

    fun safetyRule(): String =
        "Protect Mayra against unauthorized tracking and data collection, but never use stealth, anti-forensics, identity concealment, " +
        "or law-enforcement/security-control evasion to make Mayra or its Owner 'undetectable'."
}
