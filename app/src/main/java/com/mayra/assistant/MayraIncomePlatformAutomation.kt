package com.mayra.assistant
object MayraIncomePlatformAutomation {
 data class Opportunity(val name:String,val aiAllowed:Boolean,val ownerApproved:Boolean)
 fun canProceed(o:Opportunity)=o.aiAllowed && o.ownerApproved
 fun requiresManual(o:Opportunity)=!o.aiAllowed
 fun rule()="Income-platform automation requires platform permission and Owner approval; no OTP, CAPTCHA, identity or platform-rule bypass and no financial transactions."
}