package com.mayra.assistant

import android.app.KeyguardManager
import android.content.Context

/**
 * Runtime gate for the phone-lock safety rule.
 *
 * Mayra must not execute voice or background work while the device is locked.
 * This is a deny-by-default helper used immediately before command execution.
 */
object DeviceSecurityGate {
    fun isDeviceLocked(context: Context): Boolean {
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            ?: return true
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            keyguard.isDeviceLocked
        } else {
            keyguard.isKeyguardLocked
        }
    }

    fun mayExecuteUserCommand(context: Context): Boolean =
        !isDeviceLocked(context)
}
