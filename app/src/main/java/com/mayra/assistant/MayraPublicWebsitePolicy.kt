package com.mayra.assistant
object MayraPublicWebsitePolicy {
 fun hostingAllowed()=true
 fun customDomainAllowed()=true
 fun publishRequiresOwnerApproval()=true
 fun rule()="Public hosting may use free GitHub-based hosting; live publishing and custom-domain changes require Owner approval."
}