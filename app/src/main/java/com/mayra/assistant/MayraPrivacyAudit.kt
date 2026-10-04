package com.mayra.assistant
object MayraPrivacyAudit {
 enum class Event { LOGIN, SESSION, PERMISSION_CHANGE, SENSITIVE_ACCESS }
 data class Record(val event:Event,val actorId:String,val timestampMs:Long,val details:String)
 fun valid(r:Record)=r.actorId.isNotBlank() && r.timestampMs>0
 fun secretSafe(details:String)= !details.contains("password",true) && !details.contains("otp",true) && !details.contains("token",true)
 fun rule()="Audit important access and permission events without storing passwords, OTPs, tokens, biometric templates or other secrets."
}