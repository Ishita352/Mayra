package com.mayra.assistant

import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object MayraQuickOwnerLink {
    private const val CODE_TTL_MS = 120_000L
    private const val KEY_CODE = "quick_owner_link_code"
    private const val KEY_EXPIRES = "quick_owner_link_expires"
    private const val KEY_OWNER_ID = "quick_owner_link_owner_id"

    data class Link(val ownerId: String, val code: String, val expiresAtMs: Long)

    fun create(ownerId: String, nowMs: Long = System.currentTimeMillis(), random: SecureRandom = SecureRandom()): Link {
        require(ownerId.isNotBlank())
        val code = (10000000 + random.nextInt(90000000)).toString()
        return Link(ownerId.trim(), code, nowMs + CODE_TTL_MS)
    }

    fun save(prefs: android.content.SharedPreferences, link: Link) {
        prefs.edit().putString(KEY_OWNER_ID, link.ownerId)
            .putString(KEY_CODE, link.code).putLong(KEY_EXPIRES, link.expiresAtMs).apply()
    }

    fun current(prefs: android.content.SharedPreferences, nowMs: Long = System.currentTimeMillis()): Link? {
        val owner = prefs.getString(KEY_OWNER_ID, null) ?: return null
        val code = prefs.getString(KEY_CODE, null) ?: return null
        val expires = prefs.getLong(KEY_EXPIRES, 0L)
        if (expires <= nowMs) { clear(prefs); return null }
        return Link(owner, code, expires)
    }

    fun clear(prefs: android.content.SharedPreferences) {
        prefs.edit().remove(KEY_OWNER_ID).remove(KEY_CODE).remove(KEY_EXPIRES).apply()
    }

    fun payload(link: Link): String =
        "MAYRA-LINK|v1|owner=${link.ownerId}|code=${link.code}|expires=${link.expiresAtMs}"

    fun fingerprint(ownerId: String, code: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(ownerId.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(code.toByteArray(Charsets.UTF_8)).take(8)
            .joinToString("") { "%02x".format(it) }
    }
}
