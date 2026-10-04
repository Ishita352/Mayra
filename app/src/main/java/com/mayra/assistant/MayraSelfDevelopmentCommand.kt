package com.mayra.assistant

import android.content.SharedPreferences

/**
 * Entry point for Mayra's continuous self-development requests.
 *
 * A voice command can create a reviewable update proposal, but it can never
 * silently approve or apply source/security changes. Applying a proposal stays
 * behind the Owner approval + test/CI gate.
 */
object MayraSelfDevelopmentCommand {
    const val PENDING_KEY = "mayra_pending_self_development"

    data class Result(
        val handled: Boolean,
        val response: String,
        val stage: MayraSelfUpdateWorkflow.Stage
    )

    fun request(prefs: SharedPreferences, description: String): Result {
        val clean = description.trim()
        if (clean.isBlank()) {
            return Result(false, "", MayraSelfUpdateWorkflow.Stage.BLOCKED)
        }

        val ownerAuthorized = prefs.getBoolean("owner_command_authorized", false)
        if (!ownerAuthorized) {
            return Result(
                true,
                "বস, Self-Development কমান্ডের আগে Owner verification প্রয়োজন।",
                MayraSelfUpdateWorkflow.Stage.OWNER_APPROVAL_REQUIRED
            )
        }

        val request = MayraSelfUpdateWorkflow.Request(
            description = clean,
            ownerApproved = false
        )
        prefs.edit()
            .putString(PENDING_KEY, clean)
            .apply()

        val stage = MayraSelfUpdateWorkflow.nextStage(request, testsPassed = false)
        return Result(
            true,
            "বস, আমি পরিবর্তনটির plan তৈরি করেছি। এখন source review → policy/security check → tests/CI → আপনার explicit Owner approval → apply হবে। Owner identity, authentication, permissions, security boundary, audit logging, sensitive data ও payment rules নিজে পরিবর্তন করা যাবে না।",
            stage
        )
    }

    fun hasPendingRequest(prefs: SharedPreferences): Boolean =
        !prefs.getString(PENDING_KEY, null).isNullOrBlank()

    fun clearPendingRequest(prefs: SharedPreferences) {
        prefs.edit().remove(PENDING_KEY).apply()
    }
}
