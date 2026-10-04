package com.mayra.assistant

import android.content.Context
import android.content.Intent

/** Owner-initiated WhatsApp handoff using Android's installed WhatsApp app. */
object MayraWhatsAppShare {
    fun buildIntent(message: MayraWhatsAppImportantChannel.Message): Intent? {
        if (!MayraWhatsAppImportantChannel.validate(message)) return null
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_SUBJECT, message.title)
            putExtra(Intent.EXTRA_TEXT, format(message))
        }
    }

    fun format(message: MayraWhatsAppImportantChannel.Message): String =
        buildString {
            append(message.title)
            append("\n\n")
            append(message.body)
            if (!message.sourceUrl.isNullOrBlank()) {
                append("\n\nSource: ")
                append(message.sourceUrl)
            }
            append("\n\n— Mayra")
        }

    fun launch(context: Context, message: MayraWhatsAppImportantChannel.Message): Boolean {
        val intent = buildIntent(message) ?: return false
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
