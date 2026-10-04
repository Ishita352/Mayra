package com.mayra.assistant

/**
 * Persistent phone-side pairing session contract for the Windows companion.
 *
 * The actual transport/agent remains responsible for accepting the code.
 * This class never treats a generated code as proof of pairing by itself.
 */
class MayraWindowsPairingSession(
    private val store: Store,
    private val clockMs: () -> Long = { System.currentTimeMillis() }
) {
    interface Store {
        fun get(key: String): String?
        fun put(key: String, value: String)
        fun remove(key: String)
    }

    data class Invite(val deviceId: String, val code: String, val expiresAtMs: Long)

    fun saveInvite(invite: Invite) {
        store.put(KEY_DEVICE, invite.deviceId)
        store.put(KEY_CODE, invite.code)
        store.put(KEY_EXPIRES, invite.expiresAtMs.toString())
    }

    fun pendingInvite(): Invite? {
        val device = store.get(KEY_DEVICE) ?: return null
        val code = store.get(KEY_CODE) ?: return null
        val expires = store.get(KEY_EXPIRES)?.toLongOrNull() ?: return null
        if (clockMs() >= expires) {
            clear()
            return null
        }
        return Invite(device, code, expires)
    }

    fun markPaired(deviceId: String) {
        require(deviceId.isNotBlank())
        store.put(KEY_PAIRED_DEVICE, deviceId)
        clear()
    }

    fun pairedDeviceId(): String? = store.get(KEY_PAIRED_DEVICE)?.takeIf { it.isNotBlank() }

    fun isPaired(): Boolean = pairedDeviceId() != null

    fun revoke() {
        store.remove(KEY_PAIRED_DEVICE)
        clear()
    }

    private fun clear() {
        store.remove(KEY_DEVICE)
        store.remove(KEY_CODE)
        store.remove(KEY_EXPIRES)
    }

    companion object {
        private const val KEY_DEVICE = "windows_pairing_device"
        private const val KEY_CODE = "windows_pairing_code"
        private const val KEY_EXPIRES = "windows_pairing_expires"
        private const val KEY_PAIRED_DEVICE = "windows_paired_device"
    }
}
