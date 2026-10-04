package com.mayra.assistant

import org.junit.Assert.*
import org.junit.Test

class MayraFounderIdentityTest {
    private class MemoryStore : MayraFounderIdentity.Store {
        private val data = mutableMapOf<String, String>()
        override fun get(key: String): String? = data[key]
        override fun put(key: String, value: String) { data[key] = value }
    }

    @Test
    fun founderProfileIsCanonical() {
        val identity = MayraFounderIdentity(MemoryStore())
        assertEquals("Gopal Basak", identity.profile().displayName)
        assertEquals("Founder / Owner", identity.profile().role)
        assertEquals("owner", identity.profile().ownerId)
    }

    @Test
    fun verifiedOwnerIsRecognizedWithoutStoringBiometricOrVoiceData() {
        val store = MemoryStore()
        val identity = MayraFounderIdentity(store)
        val result = identity.recognizeVerifiedOwner(
            MayraFounderIdentity.VerificationMethod.ANDROID_BIOMETRIC
        )

        assertTrue(result.recognized)
        assertEquals("Gopal Basak", result.profile.displayName)
        assertEquals(
            MayraFounderIdentity.VerificationMethod.ANDROID_BIOMETRIC,
            identity.lastVerificationMethod()
        )
        assertTrue(identity.isCurrentlyRecognized())
    }

    @Test
    fun recognitionCanBeCleared() {
        val identity = MayraFounderIdentity(MemoryStore())
        identity.recognizeVerifiedOwner(
            MayraFounderIdentity.VerificationMethod.EXPLICIT_OWNER_CONFIRMATION
        )
        identity.clearRecognition()
        assertFalse(identity.isCurrentlyRecognized())
        assertNull(identity.lastVerificationMethod())
    }
}
