package com.mayra.assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

object MayraPublicInfoLookup {
    fun openWhatsAppNumberPublicSearch(context: Context, number: String) {
        val q = number.trim()
        require(q.isNotBlank())
        openSearch(context, "public information about WhatsApp number " + q)
    }

    fun openTelegramChannelPublicSearch(context: Context, channel: String) {
        val q = channel.trim()
        require(q.isNotBlank())
        openSearch(context, "public information about Telegram channel " + q)
    }

    private fun openSearch(context: Context, query: String) {
        val encoded = URLEncoder.encode(query, "UTF-8")
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + encoded)))
    }
}
