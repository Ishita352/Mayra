package com.mayra.assistant
object MayraOwnerSkillMastery {
 data class Skill(val name:String,val verified:Boolean,val evidence:String)
 fun verifiedSkills(skills:List<Skill>)=skills.filter{it.verified && it.evidence.isNotBlank()}
 fun rule()="Owner skill mastery requires evidence; Mayra never invents skills, certificates or experience."
}