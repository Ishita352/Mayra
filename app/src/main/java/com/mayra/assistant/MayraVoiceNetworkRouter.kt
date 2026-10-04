package com.mayra.assistant

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** Gate 2: explicit online/offline routing for voice-controlled workflows. */
object MayraVoiceNetworkRouter {
    enum class Mode { OFFLINE, ONLINE }

    enum class Route {
        LOCAL_VOICE,
        LOCAL_OFFLINE_TASK,
        ONLINE_TASK,
        QUEUE_FOR_LATER,
        OWNER_APPROVAL_REQUIRED
    }

    fun mode(context: Context): Mode {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return Mode.OFFLINE
        val network = manager.activeNetwork ?: return Mode.OFFLINE
        val caps = manager.getNetworkCapabilities(network) ?: return Mode.OFFLINE
        return if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) Mode.ONLINE else Mode.OFFLINE
    }

    fun route(task: MayraOfflineVoiceWorkflow.Task, context: Context, ownerApproved: Boolean = false): Route {
        val online = mode(context) == Mode.ONLINE
        return when (MayraOfflineVoiceWorkflow.decide(task, online, ownerApproved)) {
            MayraOfflineVoiceWorkflow.Decision.ALLOW_OFFLINE -> Route.LOCAL_OFFLINE_TASK
            MayraOfflineVoiceWorkflow.Decision.QUEUE_FOR_LATER -> Route.QUEUE_FOR_LATER
            MayraOfflineVoiceWorkflow.Decision.OWNER_APPROVAL_REQUIRED -> Route.OWNER_APPROVAL_REQUIRED
            MayraOfflineVoiceWorkflow.Decision.NETWORK_REQUIRED -> if (online) Route.ONLINE_TASK else Route.QUEUE_FOR_LATER
            MayraOfflineVoiceWorkflow.Decision.BLOCKED -> Route.OWNER_APPROVAL_REQUIRED
        }
    }

    fun statusMessage(context: Context): String =
        if (mode(context) == Mode.ONLINE)
            "বস, Mayra এখন Online। Network-required কাজের জন্য online route ব্যবহার করতে পারবে।"
        else
            "বস, Mayra এখন Offline। Local কাজ চলবে; cloud/network কাজ queue হবে।"
}
