package com.mayra.assistant

import org.junit.Assert.assertTrue
import org.junit.Test

class CareerProfileWorkflowTest {
    @Test
    fun plansCvBuildWithoutInventingCredentials() {
        val result = CareerProfileWorkflow.plan("আমার CV তৈরি করো")
        assertTrue(result.recognized)
        assertTrue(CareerProfileWorkflow.Action.VIEW_PROFILE in result.actions)
        assertTrue(CareerProfileWorkflow.Action.BUILD_CV in result.actions)
    }

    @Test
    fun plansJobMatchAndSkillGap() {
        val result = CareerProfileWorkflow.plan("job matching করে skill gap খুঁজে দাও")
        assertTrue(CareerProfileWorkflow.Action.MATCH_JOB in result.actions)
        assertTrue(CareerProfileWorkflow.Action.IDENTIFY_SKILL_GAP in result.actions)
    }

    @Test
    fun unknownRequestFailsClosed() {
        assertTrue(!CareerProfileWorkflow.plan("weather").recognized)
    }
}
