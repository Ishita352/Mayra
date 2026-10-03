package com.mayra.assistant

/**
 * Safe cyber-learning and prank simulation policy.
 *
 * These features are simulations or sandbox exercises only. They never target
 * third-party devices, accounts, networks, credentials, or security controls.
 */
object FunCyberLabPolicy {
    enum class Mode {
        FAKE_HACKER_TERMINAL,
        PASSWORD_CRACK_SIMULATION,
        FAKE_NETWORK_SCAN,
        FAKE_HACKED_SCREEN,
        CYBER_CHALLENGE,
        CTF_SANDBOX,
        SECURITY_QUIZ
    }

    fun modes(): List<Mode> = Mode.values().toList()

    fun isSimulationOnly(): Boolean = true
    fun mayTargetThirdPartyDevice(): Boolean = false
    fun mayCollectRealCredentials(): Boolean = false
    fun mayRunRealPasswordCracking(): Boolean = false
    fun mayScanThirdPartyNetwork(): Boolean = false
    fun mayBypassSecurityControl(): Boolean = false
    fun mayDeployMalwareOrSpyware(): Boolean = false
    fun mayCaptureOtpOrAuthenticationSecrets(): Boolean = false
}
