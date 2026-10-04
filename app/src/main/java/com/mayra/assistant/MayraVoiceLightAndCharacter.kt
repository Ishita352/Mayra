package com.mayra.assistant

object MayraVoiceLightAndCharacter {
 data class CharacterConfig(val enabled:Boolean=false,val name:String="Mayra",val voiceLight:Boolean=true)
 fun validate(c:CharacterConfig)=c.name.isNotBlank()
 fun safetyNotice()="Optional character/voice presentation only; no covert microphone/camera access and no biometric identity inference."
}