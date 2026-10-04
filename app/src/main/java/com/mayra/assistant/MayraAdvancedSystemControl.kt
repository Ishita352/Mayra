package com.mayra.assistant
object MayraAdvancedSystemControl {
 enum class Action { APP, FILE, DISPLAY, SOUND, NETWORK, SYSTEM_SETTING, FINANCIAL_TRANSACTION }
 fun allowed(action:Action)=action!=Action.FINANCIAL_TRANSACTION
 fun rule()="Advanced system control remains allowlisted, permission-bound and Owner-controlled; financial transactions are permanently blocked."
}