package com.mayra.assistant
object MayraWindowsSecurePairing {
 data class PairingRequest(val deviceId:String,val ownerVerified:Boolean,val bootstrapCompleted:Boolean)
 fun approve(r:PairingRequest)=r.deviceId.isNotBlank() && r.ownerVerified && r.bootstrapCompleted
 fun rule()="Windows pairing requires Owner verification and completed Android bootstrap; pairing secrets are never exposed in logs."
}