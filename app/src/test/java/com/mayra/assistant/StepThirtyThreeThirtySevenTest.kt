package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class StepThirtyThreeThirtySevenTest {
 @Test fun websitePublishingNeedsOwnerApproval(){assertTrue(MayraPublicWebsitePolicy.publishRequiresOwnerApproval())}
 @Test fun websiteIntegrationRequiresOwner(){assertTrue(MayraWebsiteIntegration.allowed(MayraWebsiteIntegration.Request(true,true,false)));assertFalse(MayraWebsiteIntegration.allowed(MayraWebsiteIntegration.Request(false,true,true)))}
 @Test fun systemControlBlocksFinancial(){assertFalse(MayraAdvancedSystemControl.allowed(MayraAdvancedSystemControl.Action.FINANCIAL_TRANSACTION))}
 @Test fun incomeAutomationNeedsPermissionAndOwner(){assertTrue(MayraIncomePlatformAutomation.canProceed(MayraIncomePlatformAutomation.Opportunity("x",true,true)));assertFalse(MayraIncomePlatformAutomation.canProceed(MayraIncomePlatformAutomation.Opportunity("x",true,false)))}
 @Test fun humanOnlyPlatformBlocksAutomation(){assertTrue(MayraIncomeHumanOnlyRules.blockedWhenHumanOnly(false));assertTrue(MayraIncomeHumanOnlyRules.automationAllowed(true))}
}