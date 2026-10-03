package com.mayra.assistant

/**
 * Safe operating contract for Mayra's OneForma assistance.
 *
 * Mayra can explain rules, organize requirements, prepare non-submissional
 * notes/checklists and help the owner navigate allowed workflows. It must not
 * manufacture work for a project that prohibits AI or automation.
 */
object OneFormaProjectAssistantPolicy {
    enum class AssistanceMode {
        DISCOVERY, PROJECT_RULE_ANALYSIS, ELIGIBILITY_PREPARATION,
        APPLICATION_PREPARATION, TRAINING_PREPARATION, HUMAN_ONLY_ASSISTANCE,
        AI_ASSISTED_WORK, BLOCKED_PENDING_RULES
    }

    enum class Action {
        READ_PUBLIC_PROJECT_INFO, READ_OWNER_PROVIDED_PROJECT_DOCUMENT,
        EXTRACT_REQUIREMENTS, CLASSIFY_AI_RULE, CHECK_ELIGIBILITY,
        PREPARE_CHECKLIST, PREPARE_NON_SUBMISSION_NOTES, OPEN_OFFICIAL_PAGE,
        TRACK_APPLICATION_STATUS, TRACK_TASK_DEADLINE, EXPLAIN_INSTRUCTIONS,
        PREPARE_ALLOWED_DRAFT, SUBMIT_WORK, COMPLETE_IDENTITY_VERIFICATION,
        SIGN_NDA, BYPASS_RESTRICTION
    }

    fun allowedActions(mode: AssistanceMode): Set<Action> = when (mode) {
        AssistanceMode.DISCOVERY -> setOf(
            Action.READ_PUBLIC_PROJECT_INFO, Action.EXTRACT_REQUIREMENTS,
            Action.CHECK_ELIGIBILITY, Action.PREPARE_CHECKLIST,
            Action.OPEN_OFFICIAL_PAGE
        )
        AssistanceMode.PROJECT_RULE_ANALYSIS -> setOf(
            Action.READ_PUBLIC_PROJECT_INFO, Action.READ_OWNER_PROVIDED_PROJECT_DOCUMENT,
            Action.EXTRACT_REQUIREMENTS, Action.CLASSIFY_AI_RULE,
            Action.PREPARE_CHECKLIST, Action.EXPLAIN_INSTRUCTIONS
        )
        AssistanceMode.ELIGIBILITY_PREPARATION -> setOf(
            Action.CHECK_ELIGIBILITY, Action.PREPARE_CHECKLIST, Action.OPEN_OFFICIAL_PAGE
        )
        AssistanceMode.APPLICATION_PREPARATION -> setOf(
            Action.CHECK_ELIGIBILITY, Action.PREPARE_CHECKLIST,
            Action.OPEN_OFFICIAL_PAGE, Action.EXPLAIN_INSTRUCTIONS
        )
        AssistanceMode.TRAINING_PREPARATION -> setOf(
            Action.EXPLAIN_INSTRUCTIONS, Action.PREPARE_CHECKLIST,
            Action.PREPARE_NON_SUBMISSION_NOTES
        )
        AssistanceMode.HUMAN_ONLY_ASSISTANCE -> setOf(
            Action.READ_PUBLIC_PROJECT_INFO, Action.READ_OWNER_PROVIDED_PROJECT_DOCUMENT,
            Action.EXTRACT_REQUIREMENTS, Action.CLASSIFY_AI_RULE,
            Action.PREPARE_CHECKLIST, Action.PREPARE_NON_SUBMISSION_NOTES,
            Action.EXPLAIN_INSTRUCTIONS, Action.OPEN_OFFICIAL_PAGE,
            Action.TRACK_APPLICATION_STATUS, Action.TRACK_TASK_DEADLINE
        )
        AssistanceMode.AI_ASSISTED_WORK -> setOf(
            Action.EXPLAIN_INSTRUCTIONS, Action.PREPARE_ALLOWED_DRAFT,
            Action.PREPARE_CHECKLIST, Action.TRACK_TASK_DEADLINE
        )
        AssistanceMode.BLOCKED_PENDING_RULES -> setOf(
            Action.READ_PUBLIC_PROJECT_INFO, Action.READ_OWNER_PROVIDED_PROJECT_DOCUMENT,
            Action.EXTRACT_REQUIREMENTS, Action.CLASSIFY_AI_RULE,
            Action.PREPARE_CHECKLIST, Action.EXPLAIN_INSTRUCTIONS
        )
    }

    fun maySubmitWork(mode: AssistanceMode): Boolean = false

    fun mayPerformIdentityVerification(mode: AssistanceMode): Boolean = false
    fun maySignNdaForOwner(mode: AssistanceMode): Boolean = false
    fun mayBypassRestriction(mode: AssistanceMode): Boolean = false
    fun mayGenerateProhibitedWork(mode: AssistanceMode): Boolean = false

    fun modeFor(access: IncomePlatformEligibilityPolicy.PlatformAccess): AssistanceMode =
        when (access) {
            IncomePlatformEligibilityPolicy.PlatformAccess.AI_ALLOWED ->
                AssistanceMode.AI_ASSISTED_WORK
            IncomePlatformEligibilityPolicy.PlatformAccess.AI_ASSISTANCE_LIMITED ->
                AssistanceMode.PROJECT_RULE_ANALYSIS
            IncomePlatformEligibilityPolicy.PlatformAccess.AI_BANNED,
            IncomePlatformEligibilityPolicy.PlatformAccess.HUMAN_ONLY ->
                AssistanceMode.HUMAN_ONLY_ASSISTANCE
            IncomePlatformEligibilityPolicy.PlatformAccess.UNKNOWN_RESTRICTED ->
                AssistanceMode.BLOCKED_PENDING_RULES
        }
}
