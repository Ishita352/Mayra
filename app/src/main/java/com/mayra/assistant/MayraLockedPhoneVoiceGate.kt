package com.mayra.assistant

import android.content.SharedPreferences

/** Locked-phone gate: only explicitly enabled, non-sensitive voice tasks may proceed. */
object MayraLockedPhoneVoiceGate {
 enum class Decision{ALLOW_LIMITED_VOICE, OWNER_VERIFICATION_REQUIRED, BLOCK}
 fun decide(prefs:SharedPreferences, ownerVerified:Boolean, masterOn:Boolean, locked:Boolean, task:String):Decision{
  if(!masterOn||task.isBlank()) return Decision.BLOCK
  if(!locked) return Decision.ALLOW_LIMITED_VOICE
  if(!LockModePolicy.isEnabled(prefs)) return Decision.BLOCK
  if(!ownerVerified) return Decision.OWNER_VERIFICATION_REQUIRED
  val t=task.lowercase()
  if(listOf("financial","payment","security change","password change","install app","delete").any{t.contains(it)}) return Decision.BLOCK
  return Decision.ALLOW_LIMITED_VOICE
 }
 fun rule()="Locked-phone voice access is optional, Owner-controlled, verification-gated, and limited to non-sensitive tasks."
}