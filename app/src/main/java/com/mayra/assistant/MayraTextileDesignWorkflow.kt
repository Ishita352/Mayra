package com.mayra.assistant

object MayraTextileDesignWorkflow {
 enum class Stage { BRIEF, CONCEPT, PALETTE, MOTIF, REVIEW, EXPORT }
 data class Design(val name:String,val brief:String,val palette:List<String>,val motifs:List<String>,val stage:Stage=Stage.BRIEF)
 fun advance(d:Design):Design=d.copy(stage=when(d.stage){Stage.BRIEF->Stage.CONCEPT;Stage.CONCEPT->Stage.PALETTE;Stage.PALETTE->Stage.MOTIF;Stage.MOTIF->Stage.REVIEW;Stage.REVIEW->Stage.EXPORT;Stage.EXPORT->Stage.EXPORT})
 fun valid(d:Design)=d.name.isNotBlank()&&d.brief.isNotBlank()
}