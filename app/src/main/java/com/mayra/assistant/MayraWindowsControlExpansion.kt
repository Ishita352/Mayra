package com.mayra.assistant
object MayraWindowsControlExpansion {
 enum class Action { OPEN_APP, MANAGE_FILE, DISPLAY, SOUND, NETWORK, SYSTEM_SETTINGS, FINANCIAL_TRANSACTION }
 fun allowed(action:Action)=action!=Action.FINANCIAL_TRANSACTION
 fun rule()="Windows control uses an explicit allowlist, OS permissions and Owner authorization; financial actions are always blocked."
}