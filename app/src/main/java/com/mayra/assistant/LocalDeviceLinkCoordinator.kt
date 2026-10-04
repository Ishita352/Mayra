package com.mayra.assistant

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONObject

/**
 * Session coordinator plus the Android-side LAN transport for the Windows companion.
 *
 * Android remains standalone: transport calls are explicit, short-lived and run
 * off the UI thread. A Windows connection is never required for normal Mayra use.
 */
class LocalDeviceLinkCoordinator(
    private val clockMs: () -> Long = { System.currentTimeMillis() },
    private val random: SecureRandom = SecureRandom()
) {
    data class PairingInvite(val deviceId: String, val code: String, val expiresAtMs: Long)
    data class Session(
        val deviceId: String,
        val capabilities: Set<NetworkDeviceControlPolicy.Capability>,
        val createdAtMs: Long,
        val securityScan: DevicePreConnectionScan
    )
    data class Endpoint(val host: String, val port: Int = 8765)
    data class TransportResult(val ok: Boolean, val response: String, val error: String? = null)

    private val pending = ConcurrentHashMap<String, PairingInvite>()
    private val sessions = ConcurrentHashMap<String, Session>()

    fun createInvite(deviceId: String, ttlMs: Long = 5 * 60 * 1000L): PairingInvite {
        require(deviceId.isNotBlank())
        require(ttlMs in 30_000L..10 * 60 * 1000L)
        val code = (100000 + random.nextInt(900000)).toString()
        val invite = PairingInvite(deviceId, code, clockMs() + ttlMs)
        pending[deviceId] = invite
        return invite
    }

    fun acceptInvite(
        deviceId: String,
        code: String,
        capabilities: Set<NetworkDeviceControlPolicy.Capability>,
        risk: DevicePreConnectionSecurityPolicy.Risk = DevicePreConnectionSecurityPolicy.Risk.UNKNOWN,
        ownerApproved: Boolean = false,
        securityScan: DevicePreConnectionScan? = null
    ): Session? {
        val scan = securityScan ?: return null
        if (scan.risk != risk || !scan.isConnectionEligible() || !ownerApproved) return null
        val invite = pending[deviceId] ?: return null
        if (clockMs() >= invite.expiresAtMs || invite.code != code) {
            pending.remove(deviceId)
            return null
        }
        if (capabilities.isEmpty()) return null
        val session = Session(deviceId, capabilities.toSet(), clockMs(), scan)
        sessions[deviceId] = session
        pending.remove(deviceId)
        return session
    }

    /**
     * Sends one bounded JSON request to the Windows agent.
     * No shell/command execution is performed on Android.
     */
    fun request(endpoint: Endpoint, payload: JSONObject, timeoutMs: Int = 5000): TransportResult {
        if (endpoint.host.isBlank() || endpoint.port !in 1..65535) {
            return TransportResult(false, "", "Invalid Windows endpoint")
        }
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(endpoint.host.trim(), endpoint.port), timeoutMs)
                socket.soTimeout = timeoutMs
                val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream(), Charsets.UTF_8), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.UTF_8))
                writer.println(payload.toString())
                val line = reader.readLine() ?: return TransportResult(false, "", "Windows agent returned no response")
                val response = JSONObject(line)
                TransportResult(response.optBoolean("ok", false), line,
                    response.optString("error").takeIf { it.isNotBlank() })
            }
        } catch (e: Exception) {
            TransportResult(false, "", "Windows connection failed: " + (e.message ?: "unknown error"))
        }
    }

    fun requestPair(endpoint: Endpoint, code: String): TransportResult =
        request(endpoint, JSONObject().put("action", "PAIR_REQUEST").put("code", code))

    fun completePair(endpoint: Endpoint, code: String): TransportResult =
        request(endpoint, JSONObject().put("action", "PAIR_APPROVE").put("code", code))

    fun command(endpoint: Endpoint, sessionToken: String, command: String): TransportResult =
        request(endpoint, JSONObject()
            .put("action", "COMMAND")
            .put("session_token", sessionToken)
            .put("command", command))

    fun status(endpoint: Endpoint, sessionToken: String? = null): TransportResult =
        request(endpoint, JSONObject().put("action", "STATUS").apply {
            if (!sessionToken.isNullOrBlank()) put("session_token", sessionToken)
        })

    fun revoke(endpoint: Endpoint, sessionToken: String): TransportResult =
        request(endpoint, JSONObject().put("action", "REVOKE").put("session_token", sessionToken))

    fun revoke(deviceId: String) {
        pending.remove(deviceId)
        sessions.remove(deviceId)
    }

    fun session(deviceId: String): Session? = sessions[deviceId]

    fun mayUse(deviceId: String, capability: NetworkDeviceControlPolicy.Capability): Boolean {
        val session = sessions[deviceId] ?: return false
        return NetworkDeviceControlPolicy.mayControlDevice(
            NetworkDeviceControlPolicy.PairingState.PAIRED,
            capability in session.capabilities
        )
    }
}
