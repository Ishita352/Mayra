package com.mayra.assistant

/**
 * Release gate for the final build.
 * CI validation and physical-device validation are tracked separately.
 */
object FinalReadinessGate {
    data class Report(
        val unitTestsPassed: Boolean,
        val apkBuildPassed: Boolean,
        val securityPolicyPassed: Boolean,
        val ownerApprovalFlowPassed: Boolean,
        val realDeviceTested: Boolean
    ) {
        val ciReleaseReady: Boolean
            get() = unitTestsPassed &&
                apkBuildPassed &&
                securityPolicyPassed &&
                ownerApprovalFlowPassed

        val fullyDeviceValidated: Boolean
            get() = ciReleaseReady && realDeviceTested
    }

    fun evaluate(
        unitTestsPassed: Boolean,
        apkBuildPassed: Boolean,
        securityPolicyPassed: Boolean,
        ownerApprovalFlowPassed: Boolean,
        realDeviceTested: Boolean
    ) = Report(
        unitTestsPassed,
        apkBuildPassed,
        securityPolicyPassed,
        ownerApprovalFlowPassed,
        realDeviceTested
    )
}
