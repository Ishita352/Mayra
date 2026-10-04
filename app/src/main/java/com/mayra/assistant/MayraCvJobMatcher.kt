package com.mayra.assistant
object MayraCvJobMatcher {
 data class Match(val role:String,val score:Int,val reasons:List<String>)
 fun match(skills:Set<String>,role:String,required:Set<String>):Match {
  val hits=required.map{it.lowercase()}.filter{r->skills.any{it.lowercase()==r}}
  return Match(role,hits.size*100/required.size.coerceAtLeast(1),hits.map{"skill:$it"})
 }
 fun rule()="Never invent skills, experience or credentials; matches use the Owner's verified profile only."
}