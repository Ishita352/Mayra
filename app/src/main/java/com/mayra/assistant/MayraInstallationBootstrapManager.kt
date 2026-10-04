package com.mayra.assistant

import android.content.Context

/** Password-free installation bootstrap; central Owner verification handles identity. */
class MayraInstallationBootstrapManager(private val context: Context) {
 companion object { private const val PREFS="mayra_secure"; private const val KEY_COMPLETED="installation_bootstrap_completed" }
 private val prefs=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
 fun isCompleted():Boolean=true
 fun installationDateLabel():String="Password disabled"
 fun verify(input:String):Boolean=true
 fun markCompleted(){prefs.edit().putBoolean(KEY_COMPLETED,true).apply()}
 fun passwordRemoved():Boolean=true
}