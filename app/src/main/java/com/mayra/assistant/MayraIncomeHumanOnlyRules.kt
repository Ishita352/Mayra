package com.mayra.assistant
object MayraIncomeHumanOnlyRules {
 fun automationAllowed(platformAllowsAi:Boolean)=platformAllowsAi
 fun blockedWhenHumanOnly(platformAllowsAi:Boolean)=!platformAllowsAi
 fun rule()="If a platform requires human-only work or prohibits automation, Mayra must stop automation and assist the Owner manually without bypassing the rule."
}