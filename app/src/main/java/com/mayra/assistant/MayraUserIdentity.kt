package com.mayra.assistant

object MayraUserIdentity {
    const val MAX_FAMILY_USERS = 10
    const val OWNER_SLOT = "owner"

    enum class Role { OWNER, FAMILY_USER }

    data class UserId(
        val id: String,
        val role: Role,
        val displayName: String
    )

    fun isValidFamilyUserId(id: String): Boolean =
        Regex("""^family-(0[1-9]|10)$""").matches(id)

    fun familyUserIds(): List<String> =
        (1..MAX_FAMILY_USERS).map { "family-" + it.toString().padStart(2, '0') }

    fun maxUserSlots(): Int = 1 + MAX_FAMILY_USERS

    fun hasFullControl(role: Role): Boolean = role == Role.OWNER

    fun mayManageUsers(role: Role): Boolean = role == Role.OWNER
}
