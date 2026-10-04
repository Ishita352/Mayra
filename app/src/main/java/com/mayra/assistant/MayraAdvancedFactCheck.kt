package com.mayra.assistant
object MayraAdvancedFactCheck {
 data class Claim(val text:String,val source:String?,val reliable:Boolean)
 data class Result(val supported:Boolean,val conflicts:Boolean,val uncertainty:Boolean)
 fun evaluate(claims:List<Claim>):Result {
  if(claims.isEmpty()) return Result(false,false,true)
  val reliable=claims.filter{it.reliable}
  return Result(reliable.isNotEmpty(), reliable.mapNotNull{it.source}.distinct().size>1 && reliable.map{it.text}.distinct().size>1, reliable.isEmpty())
 }
 fun rule()="Cross-check important video/information claims against multiple reliable public sources; show uncertainty and conflicts instead of inventing certainty."
}