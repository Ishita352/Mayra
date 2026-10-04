package com.mayra.assistant

object MayraSocialIntelligencePolicy {
 enum class Platform { WHATSAPP, TELEGRAM, FACEBOOK }
 data class Request(val platform:Platform,val userAuthorized:Boolean,val aiAllowed:Boolean,val action:String)
 fun decide(r:Request):String = when {
  !r.userAuthorized -> "BLOCKED: user authorization required"
  !r.aiAllowed -> "MANUAL_ONLY: platform rules do not permit automation"
  r.action.lowercase().contains("otp") || r.action.lowercase().contains("captcha") -> "BLOCKED: identity/verification bypass is prohibited"
  else -> "ASSISTED: read, summarize, draft, organize, or respond only within granted permissions"
 }
}