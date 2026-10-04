package com.mayra.assistant

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.VideoProfile

/**
 * Android-supported incoming-call integration.
 *
 * The OS/telecom framework controls whether Mayra is allowed to act as an
 * InCallService on a particular device. Mayra never bypasses the lock screen,
 * carrier controls, or telecom permissions.
 */
class MayraInCallService : InCallService() {
    private val handler = Handler(Looper.getMainLooper())
    private val pending = mutableMapOf<Call, Runnable>()

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        if (call.state == Call.STATE_RINGING) scheduleAnswer(call)
        call.registerCallback(callback)
    }

    override fun onCallRemoved(call: Call) {
        cancel(call)
        call.unregisterCallback(callback)
        super.onCallRemoved(call)
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            if (state == Call.STATE_RINGING) scheduleAnswer(call) else cancel(call)
        }
    }

    private fun scheduleAnswer(call: Call) {
        cancel(call)
        val prefs = getSharedPreferences("mayra_secure", MODE_PRIVATE)
        if (!MayraIncomingCallPolicy.mayAutoAnswer(
                prefs.getBoolean("master_on", true),
                FeatureToggleRegistry.isEnabled(
                    prefs,
                    FeatureToggleRegistry.INCOMING_CALL_ASSISTANT
                ),
                FeatureToggleRegistry.isEnabled(
                    prefs,
                    FeatureToggleRegistry.SECURITY
                )
            )
        ) return

        val runnable = Runnable {
            val current = getSharedPreferences("mayra_secure", MODE_PRIVATE)
            val allowed = MayraIncomingCallPolicy.mayAutoAnswer(
                current.getBoolean("master_on", true),
                FeatureToggleRegistry.isEnabled(
                    current,
                    FeatureToggleRegistry.INCOMING_CALL_ASSISTANT
                ),
                FeatureToggleRegistry.isEnabled(
                    current,
                    FeatureToggleRegistry.SECURITY
                )
            )
            if (allowed && call.state == Call.STATE_RINGING) {
                call.answer(VideoProfile.STATE_AUDIO_ONLY)
            }
            pending.remove(call)
        }
        pending[call] = runnable
        handler.postDelayed(
            runnable,
            MayraIncomingCallPolicy.answerDelayMs()
        )
    }

    private fun cancel(call: Call) {
        pending.remove(call)?.let(handler::removeCallbacks)
    }

    override fun onDestroy() {
        pending.values.forEach(handler::removeCallbacks)
        pending.clear()
        super.onDestroy()
    }
}
