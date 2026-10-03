package com.mayra.assistant

import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

/**
 * Session-level coordinator for Mayra-to-Mayra LAN links.
 *
 * The transport may be Wi-Fi or a phone hotspot. Network reachability is not
 * authorization; a short-lived pairing code and explicit capability grant are
 * required before a session becomes usable.
 */
class LocalDeviceLinkCoordinator(
    private val clockMs: () -> Long = { System.currentTimeMillis() },
    private val random: SecureRandom = SecureRandom()
) {
    data class PairingInvite(
        val deviceId: String,
        val code: String,
        val expiresAtMs: Long
    )

    data class Session(
        val deviceId: String,
        val capabilities: Set<NetworkDeviceControlPolicy.Capability>,
        val createdAtMs: Long,
        val securityScan: DevicePreConnectionScan
    )

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
        ownerApproved: Boolean = false
    ): Session? {
        if (risk != DevicePreConnectionSecurityPolicy.Risk.CLEAN || !ownerApproved) return null
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
