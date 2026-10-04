package com.mayra.assistant

/**
 * Persistent phone-side Windows companion session.
 *
 * A pairing code is only a bootstrap factor. A paired session additionally
 * stores the Windows endpoint and authenticated session token.
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

    fun markPaired(deviceId: String, host: String, port: Int, token: String) {
        require(deviceId.isNotBlank() && host.isNotBlank() && port in 1..65535 && token.isNotBlank())
        store.put(KEY_PAIRED_DEVICE, deviceId)
        store.put(KEY_HOST, host)
        store.put(KEY_PORT, port.toString())
        store.put(KEY_TOKEN, token)
        clear()
    }

    fun pairedDeviceId(): String? = store.get(KEY_PAIRED_DEVICE)?.takeIf { it.isNotBlank() }
    fun pairedHost(): String? = store.get(KEY_HOST)?.takeIf { it.isNotBlank() }
    fun pairedPort(): Int = store.get(KEY_PORT)?.toIntOrNull() ?: 8765
    fun sessionToken(): String? = store.get(KEY_TOKEN)?.takeIf { it.isNotBlank() }
    fun isPaired(): Boolean = pairedDeviceId() != null

    fun revoke() {
        store.remove(KEY_PAIRED_DEVICE)
        store.remove(KEY_HOST)
        store.remove(KEY_PORT)
        store.remove(KEY_TOKEN)
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
        private const val KEY_HOST = "windows_paired_host"
        private const val KEY_PORT = "windows_paired_port"
        private const val KEY_TOKEN = "windows_paired_token"
    }
}
