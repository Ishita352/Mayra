package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IncomeBackgroundEnginePolicyTest {
    @Test
    fun allCoreIncomeModesArePermanentAndActive() {
        assertEquals(
            setOf(
                IncomeBackgroundEnginePolicy.IncomeMode.PASSIVE_INCOME_DISCOVERY,
                IncomeBackgroundEnginePolicy.IncomeMode.ACTIVE_INCOME_DISCOVERY,
                IncomeBackgroundEnginePolicy.IncomeMode.WORK_OPPORTUNITY_DISCOVERY,
                IncomeBackgroundEnginePolicy.IncomeMode.SKILL_TO_INCOME_DISCOVERY
            ),
            IncomeBackgroundEnginePolicy.backgroundModes().toSet()
        )
        assertTrue(IncomeBackgroundEnginePolicy.continuousSearchAllowed())
    }

    @Test
    fun coreIncomeModesKeepSafetyBoundaries() {
        assertTrue(IncomeBackgroundEnginePolicy.allowedBackgroundActions().isNotEmpty())
        assertTrue(!IncomeBackgroundEnginePolicy.mayPerformFinancialAction())
        assertTrue(!IncomeBackgroundEnginePolicy.mayBypassHumanOnlyWork())
        assertTrue(!IncomeBackgroundEnginePolicy.mayUseStealthExecution())
    }
}
