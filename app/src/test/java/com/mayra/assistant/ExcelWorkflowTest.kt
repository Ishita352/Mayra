package com.mayra.assistant

import org.junit.Assert.assertTrue
import org.junit.Test

class ExcelWorkflowTest {
    @Test
    fun plansCleaningFormulaAndPivot() {
        val result = ExcelWorkflow.plan("Excel data cleaning, formula and Pivot Table")
        assertTrue(result.recognized)
        assertTrue(ExcelWorkflow.Action.CLEANING in result.actions)
        assertTrue(ExcelWorkflow.Action.FORMULA in result.actions)
        assertTrue(ExcelWorkflow.Action.PIVOT in result.actions)
    }

    @Test
    fun plansBengaliAnalysisAndChart() {
        val result = ExcelWorkflow.plan("ডেটা বিশ্লেষণ করে চার্ট বানাও")
        assertTrue(result.recognized)
        assertTrue(ExcelWorkflow.Action.ANALYSIS in result.actions)
        assertTrue(ExcelWorkflow.Action.CHART in result.actions)
    }

    @Test
    fun unknownRequestFailsClosed() {
        assertTrue(!ExcelWorkflow.plan("weather").recognized)
    }
}
