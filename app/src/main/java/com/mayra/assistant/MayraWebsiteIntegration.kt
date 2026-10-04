package com.mayra.assistant
object MayraWebsiteIntegration {
 data class Request(val ownerVerified:Boolean,val chatEnabled:Boolean,val computerActionsEnabled:Boolean)
 fun allowed(r:Request)=r.ownerVerified && r.chatEnabled
 fun computerActionsAllowed(r:Request)=r.ownerVerified && r.computerActionsEnabled
 fun rule()="Website Mayra access is Owner-controlled; computer actions require explicit Owner authorization and central security gates."
}