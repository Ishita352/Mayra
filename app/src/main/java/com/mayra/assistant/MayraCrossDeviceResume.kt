package com.mayra.assistant

/** Deterministic cross-device resume envelope; actual transport remains pairing-gated. */
object MayraCrossDeviceResume {
 data class Envelope(val taskId:String,val title:String,val context:String,val sourceDevice:String,val version:Long)
 enum class Decision { ALLOW_RESUME, OWNER_APPROVAL_REQUIRED, BLOCK }
 fun create(taskId:String,title:String,context:String,sourceDevice:String,version:Long):Envelope? =
  if(taskId.isBlank()||title.isBlank()||sourceDevice.isBlank()) null else Envelope(taskId,title,context.take(20000),sourceDevice,version)
 fun decide(e:Envelope?, ownerVerified:Boolean, paired:Boolean):Decision =
  if(e==null) Decision.BLOCK else if(ownerVerified&&paired) Decision.ALLOW_RESUME else Decision.OWNER_APPROVAL_REQUIRED
 fun rule()="Resume context may cross paired devices only after Owner verification; secrets and authentication material are excluded."
}
