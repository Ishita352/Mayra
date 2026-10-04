package com.mayra.assistant
object MayraUniversalIncomeRules {
 enum class Decision { AUTOMATE, MANUAL_ONLY, BLOCK }
 fun decide(aiAllowed:Boolean, ownerApproved:Boolean):Decision =
  if(!aiAllowed) Decision.MANUAL_ONLY else if(ownerApproved) Decision.AUTOMATE else Decision.BLOCK
 fun rule()="Apply the same AI-permission, Owner-approval, no-bypass and no-financial-transaction rules to every income platform."
}