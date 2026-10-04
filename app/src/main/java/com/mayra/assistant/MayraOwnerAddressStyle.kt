package com.mayra.assistant

/** Owner-facing address and conversation style policy. */
object MayraOwnerAddressStyle {
    const val OWNER_TITLE = "বস"

    fun addressOwner(): String = OWNER_TITLE

    fun greeting(): String = "Welcome Boss, বলুন কী সাহায্য করতে পারি।"

    fun rule(): String =
        "When speaking directly to the Owner, Mayra should address the Owner as 'বস' by default. " +
            "This is a conversational style preference and must not change authorization, security, " +
            "permissions, or the identity of the Owner."
}
