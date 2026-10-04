package com.mayra.assistant

import android.content.SharedPreferences

/**
 * Central Owner-permission vs pre-approved background-automation policy.
 *
 * New/interactive work always requires explicit Owner approval.
 * Only automation types explicitly enabled by the Owner may run automatically.
 */
object MayraActionPermissionPolicy {
    enum class ActionType { NEW_TASK, SENSITIVE_TASK, DESTRUCTIVE_TASK, BACKGROUND_AUTOMATION }
    enum class Decision { OWNER_APPROVAL_REQUIRED, AUTOMATION_ALLOWED, BLOCKED }

    enum class Automation(val key: String) {
        KNOWLEDGE_REFRESH("knowledge_refresh"),
        YOUTUBE_LEARNING("youtube_learning"),
        SKILL_PRACTICE("skill_practice"),
        JOB_DISCOVERY("job_discovery"),
        INCOME_DISCOVERY("income_discovery"),
        MARKET_RULE_REFRESH("market_rule_refresh"),
        DEVICE_HEALTH_SCAN("device_health_scan")
    }

    fun decide(action: ActionType, ownerApproved: Boolean, automationEnabled: Boolean): Decision {
        if (action == ActionType.BACKGROUND_AUTOMATION && automationEnabled) {
            return Decision.AUTOMATION_ALLOWED
        }
        if (action == ActionType.BACKGROUND_AUTOMATION && !automationEnabled) {
            return Decision.OWNER_APPROVAL_REQUIRED
        }
        if (ownerApproved) return Decision.AUTOMATION_ALLOWED
        return Decision.OWNER_APPROVAL_REQUIRED
    }

    fun setAutomation(prefs: SharedPreferences, automation: Automation, enabled: Boolean) {
        prefs.edit().putBoolean("mayra_auto_" + automation.key, enabled).apply()
    }

    fun isAutomationEnabled(prefs: SharedPreferences, automation: Automation): Boolean =
        prefs.getBoolean("mayra_auto_" + automation.key, false)

    fun automationVoiceCommand(spoken: String): Pair<Automation, Boolean>? {
        val s = spoken.lowercase()
        val enabled = !(s.contains("বন্ধ") || s.contains("off") || s.contains("disable") || s.contains("stop"))
        val automation = when {
            s.contains("knowledge") || s.contains("জ্ঞান") -> Automation.KNOWLEDGE_REFRESH
            s.contains("youtube") || s.contains("ইউটিউব") -> Automation.YOUTUBE_LEARNING
            s.contains("skill") || s.contains("স্কিল") -> Automation.SKILL_PRACTICE
            s.contains("job") || s.contains("চাকরি") -> Automation.JOB_DISCOVERY
            s.contains("income") || s.contains("ইনকাম") -> Automation.INCOME_DISCOVERY
            s.contains("market") || s.contains("মার্কেট") -> Automation.MARKET_RULE_REFRESH
            s.contains("device") || s.contains("ডিভাইস") || s.contains("health scan") -> Automation.DEVICE_HEALTH_SCAN
            else -> null
        }
        return automation?.let { it to enabled }
    }

    fun rule() =
        "Mayra never starts a new user task by itself. Explicitly Owner-enabled background automations may run automatically, but cannot grant themselves permissions, bypass security, publish, submit applications, perform financial transactions, or change security controls. Owner can stop them by voice at any time."
}
