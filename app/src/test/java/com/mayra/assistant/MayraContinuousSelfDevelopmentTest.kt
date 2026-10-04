package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MayraContinuousSelfDevelopmentTest {
    @Test
    fun defaultStateEnablesAllAutomations() {
        assertEquals(
            MayraContinuousSelfDevelopment.Automation.entries.toSet(),
            MayraContinuousSelfDevelopment.defaultState().enabled
        )
    }

    @Test
    fun pausedAutomationAppearsInReminderWithoutAutoResume() {
        val now = 1_000L
        val state = MayraContinuousSelfDevelopment.pause(
            MayraContinuousSelfDevelopment.defaultState(),
            MayraContinuousSelfDevelopment.Automation.JOB_DISCOVERY,
            now
        )
        val reminders = MayraContinuousSelfDevelopment.reminders(state, now + 1)
        assertTrue(reminders.any { it.automation == MayraContinuousSelfDevelopment.Automation.JOB_DISCOVERY })
        assertTrue(MayraContinuousSelfDevelopment.Automation.JOB_DISCOVERY !in state.enabled)
    }

    @Test
    fun voiceCommandsIncludeGlobalPauseAndResume() {
        val commands = MayraContinuousSelfDevelopment.voiceCommands().joinToString(" ")
        assertTrue(commands.contains("সব self-development automation চালু"))
        assertTrue(commands.contains("self development বন্ধ"))
    }

    @Test
    fun financialTransactionsAndSilentChangesAreForbiddenByRule() {
        assertTrue(MayraContinuousSelfDevelopment.backgroundRule().contains("financial transactions"))
        assertTrue(MayraContinuousSelfDevelopment.updateApprovalRule().contains("explicit Owner approval"))
    }
}
