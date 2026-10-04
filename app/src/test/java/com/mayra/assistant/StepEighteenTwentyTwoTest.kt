package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class StepEighteenTwentyTwoTest {
 @Test fun mapContextIsInformational(){assertEquals("Kolkata",MayraMapVisualContext.create("Kolkata").place)}
 @Test fun microEarningBlocksFinancial(){assertFalse(MayraResearchMicroEarning.allowed(MayraResearchMicroEarning.Action.FINANCIAL_TRANSACTION))}
 @Test fun cvMatcherUsesVerifiedSkills(){assertEquals(100,MayraCvJobMatcher.match(setOf("Kotlin"),"Android",setOf("Kotlin")).score)}
 @Test fun androidSurfacesRequirePermission(){assertTrue(MayraAndroidIntegrationPolicy.permissionRequired(MayraAndroidIntegrationPolicy.Surface.CAMERA))}
 @Test fun securityBaselineHasNoFinancialAccess(){assertTrue(MayraAndroidSecurityTestPolicy.baseline().first{it.name=="financial transaction block"}.passed)}
}