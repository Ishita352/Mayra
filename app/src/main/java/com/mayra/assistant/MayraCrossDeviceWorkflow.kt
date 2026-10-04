package com.mayra.assistant
object MayraCrossDeviceWorkflow {
 data class Request(val ownerVerified:Boolean,val phoneReady:Boolean,val windowsPaired:Boolean,val action:String)
 fun allowed(r:Request)=r.ownerVerified && r.phoneReady && r.windowsPaired && r.action.isNotBlank()
 fun rule()="Phone-to-Windows workflows require verified Owner authorization and secure pairing; sensitive actions remain subject to the central authorization gate."
}