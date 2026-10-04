package com.mayra.assistant
object MayraResearchMicroEarning {
 enum class Action { RESEARCH, COMPARE, NOTIFY, MANUAL_APPLY, FINANCIAL_TRANSACTION }
 fun allowed(action:Action)=action!=Action.FINANCIAL_TRANSACTION
 fun rule()="Research and opportunity discovery are allowed; platform rules, human-only requirements and all financial transactions remain enforced."
}