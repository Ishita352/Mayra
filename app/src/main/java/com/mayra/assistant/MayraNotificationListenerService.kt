package com.mayra.assistant

import android.app.Notification
import android.app.NotificationListenerService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class MayraNotificationListenerService : NotificationListenerService() {
    companion object {
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private var pendingReply: PendingReply? = null

        data class PendingReply(
            val action: Notification.Action,
            val sourcePackage: String
        )

        fun replyFromOwnerCommand(context: Context, text: String): Boolean {
            val pending = pendingReply ?: return false
            if (text.isBlank() || pending.sourcePackage != WHATSAPP_PACKAGE) return false
            val remoteInputs = pending.action.remoteInputs ?: return false
            val fillIn = Intent()
            val results = Bundle()
            for (input in remoteInputs) results.putCharSequence(input.resultKey, text)
            android.app.RemoteInput.addResultsToIntent(remoteInputs, fillIn, results)
            return try {
                pending.action.actionIntent.send(context, 0, fillIn)
                pendingReply = null
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    private var tts: TextToSpeech? = null

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.setLanguage(Locale("bn", "IN"))
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != WHATSAPP_PACKAGE) return
        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        val replyAction = sbn.notification.actions
            ?.firstOrNull { it.remoteInputs?.isNotEmpty() == true }
        if (replyAction != null) {
            pendingReply = PendingReply(replyAction, sbn.packageName)
        }

        val spoken = if (title.isNotBlank() && text.isNotBlank()) {
            "WhatsApp message from " + title + ". " + text
        } else {
            "New WhatsApp message. " + title.ifBlank { text }
        }
        tts?.speak(spoken, TextToSpeech.QUEUE_FLUSH, null, "mayra_whatsapp_incoming")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName == WHATSAPP_PACKAGE) pendingReply = null
    }

    override fun onDestroy() {
        tts?.shutdown()
        tts = null
        pendingReply = null
        super.onDestroy()
    }
}
