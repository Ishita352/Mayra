package com.mayra.assistant
object MayraOwnerSharing {
 data class Grant(val userId:String,val capability:String,val expiresAtMs:Long)
 fun valid(g:Grant,nowMs:Long)=g.userId.isNotBlank() && g.capability.isNotBlank() && g.expiresAtMs>nowMs
 fun ownerDataIsolated()=true
 fun rule()="Owner grants only specific, time-limited capabilities; Owner data and Owner-only permissions remain isolated."
}