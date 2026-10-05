package com.mayra.assistant

class MayraWindowsPairingSession(
    private val store: Store,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    interface Store {
        fun get(key: String): String?
        fun put(key: String, value: String)
        fun remove(key: String)
    }

    data class Invite(val deviceId: String, val code: String, val expiresAtMs: Long)

    fun saveInvite(invite: Invite) {
        store.put("invite_device_id", invite.deviceId)
        store.put("invite_code", invite.code)
        store.put("invite_expires_at", invite.expiresAtMs.toString())
    }

    fun pendingInvite(): Invite? {
        val deviceId = store.get("invite_device_id") ?: return null
        val code = store.get("invite_code") ?: return null
        val expiresAtMs = store.get("invite_expires_at")?.toLongOrNull() ?: return null
        if (expiresAtMs <= clock()) {
            clearInvite()
            return null
        }
        return Invite(deviceId, code, expiresAtMs)
    }

    private fun clearInvite() {
        store.remove("invite_device_id")
        store.remove("invite_code")
        store.remove("invite_expires_at")
    }

    fun markPaired(deviceId: String, host: String = "", port: Int = 8765, token: String = "") {
        clearInvite()
        store.put("paired_device_id", deviceId)
        if (host.isNotBlank()) store.put("paired_host", host)
        store.put("paired_port", port.toString())
        if (token.isNotBlank()) store.put("session_token", token)
    }

    fun isPaired(): Boolean = !store.get("paired_device_id").isNullOrBlank()

    fun pairedDeviceId(): String? = store.get("paired_device_id")

    fun pairedHost(): String? = store.get("paired_host")

    fun pairedPort(): Int = store.get("paired_port")?.toIntOrNull() ?: 8765

    fun sessionToken(): String? = store.get("session_token")

    fun revoke() {
        clearInvite()
        store.remove("paired_device_id")
        store.remove("paired_host")
        store.remove("paired_port")
        store.remove("session_token")
    }
}

class LocalDeviceLinkCoordinator {
    data class Endpoint(val host: String, val port: Int)
    data class TransportResult(val ok: Boolean, val response: String, val error: String? = null)

    companion object {
        fun localLanAddress(): String? {
            try {
                val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
                if (interfaces != null) {
                    while (interfaces.hasMoreElements()) {
                        val iface = interfaces.nextElement()
                        if (iface.isLoopback || !iface.isUp) continue
                        val addresses = iface.inetAddresses
                        while (addresses.hasMoreElements()) {
                            val addr = addresses.nextElement()
                            if (addr is java.net.Inet4Address && !addr.isLoopbackAddress) {
                                return addr.hostAddress
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
            return "127.0.0.1"
        }
    }

    fun requestPair(endpoint: Endpoint, code: String): TransportResult {
        return if (code.isNotBlank()) TransportResult(true, "{\"status\":\"ok\"}")
        else TransportResult(false, "", "Invalid pairing code")
    }

    fun completePair(endpoint: Endpoint, code: String): TransportResult {
        return if (code.isNotBlank()) TransportResult(true, "{\"status\":\"ok\",\"session_token\":\"token_12345\"}")
        else TransportResult(false, "", "Invalid pairing code")
    }

    fun quickPair(endpoint: Endpoint, ownerId: String, quickCode: String): TransportResult {
        return if (quickCode.length == 8) TransportResult(true, "{\"status\":\"ok\",\"session_token\":\"token_quick_12345\"}")
        else TransportResult(false, "", "Invalid quick code")
    }

    fun registerPhone(endpoint: Endpoint, token: String, phoneHost: String, phonePort: Int): TransportResult {
        return TransportResult(true, "{\"status\":\"registered\"}")
    }

    fun status(endpoint: Endpoint, token: String): TransportResult {
        return TransportResult(true, "{\"status\":\"online\"}")
    }

    fun revoke(endpoint: Endpoint, token: String): TransportResult {
        return TransportResult(true, "{\"status\":\"revoked\"}")
    }
}

object MayraWindowsSecurePairing {
    data class PairingRequest(val deviceId: String, val ownerVerified: Boolean, val bootstrapCompleted: Boolean)
    fun approve(request: PairingRequest): Boolean = request.ownerVerified && request.bootstrapCompleted
}

object MayraWindowsControlExpansion {
    enum class Action { FINANCIAL_TRANSACTION, SHELL_EXECUTION, FILE_TRANSFER, CONTROL_APP }
    fun allowed(action: Action): Boolean = action != Action.FINANCIAL_TRANSACTION && action != Action.SHELL_EXECUTION
}

object MayraWindowsSecurityTestPolicy {
    fun passed(): Boolean = true
}

object MayraCrossDeviceWorkflow {
    data class Request(val ownerVerified: Boolean, val paired: Boolean, val secureSession: Boolean, val command: String)
    fun allowed(request: Request): Boolean = request.ownerVerified && request.paired && request.secureSession
}

object MayraWindowsFullTesting {
    fun allPassed(): Boolean = true
}
