package com.mayra.assistant

object MayraCentralMemorySyncCoordinator {
    data class SyncDecision(
        val vaultDecision: MayraCentralMemoryVault.Decision,
        val uploadDecision: MayraOfflineVoiceWorkflow.Decision
    )
    fun decide(item: MayraCentralMemoryVault.Item, config: MayraCentralMemoryVault.Config, online: Boolean, ownerApprovedForItem: Boolean = false): SyncDecision {
        val vault=MayraCentralMemoryVault.decision(item,config,ownerApprovedForItem)
        val upload=when(vault){
            MayraCentralMemoryVault.Decision.ELIGIBLE_FOR_SYNC -> MayraOfflineVoiceWorkflow.decide(MayraOfflineVoiceWorkflow.Task.CLOUD_UPLOAD,online,ownerApprovedForItem)
            MayraCentralMemoryVault.Decision.DO_NOT_SAVE, MayraCentralMemoryVault.Decision.BLOCKED -> MayraOfflineVoiceWorkflow.Decision.BLOCKED
            MayraCentralMemoryVault.Decision.ASK_OWNER_IMPORTANCE, MayraCentralMemoryVault.Decision.OWNER_APPROVAL_REQUIRED -> MayraOfflineVoiceWorkflow.Decision.OWNER_APPROVAL_REQUIRED
        }
        return SyncDecision(vault,upload)
    }
    fun rule()="Importance is checked first; every cloud item needs explicit Owner approval; offline approved work waits locally for connectivity."
}
