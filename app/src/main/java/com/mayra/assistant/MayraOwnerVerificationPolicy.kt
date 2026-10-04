package com.mayra.assistant

object MayraOwnerVerificationPolicy {
    enum class Method {
        DEVICE_CREDENTIAL,
        ANDROID_BIOMETRIC,
        CONSENTED_VOICE_VERIFICATION,
        CONSENTED_CAMERA_VERIFICATION
    }

    fun acceptedOwnerMethods(): Set<Method> =
        setOf(
            Method.DEVICE_CREDENTIAL,
            Method.ANDROID_BIOMETRIC,
            Method.CONSENTED_VOICE_VERIFICATION,
            Method.CONSENTED_CAMERA_VERIFICATION
        )

    fun mayUseSilentCameraBiometricScan(): Boolean = false
    fun mayUseSilentVoiceBiometricScan(): Boolean = false

    fun mayVerifyWithDeviceCredential(): Boolean = true
    fun mayVerifyWithAndroidBiometric(): Boolean = true

    fun mayVerifyWithVoice(consentGiven: Boolean): Boolean = consentGiven
    fun mayVerifyWithCamera(consentGiven: Boolean): Boolean = consentGiven

    fun successfulVerificationMeansOwner(method: Method): Boolean =
        method in acceptedOwnerMethods()
}
