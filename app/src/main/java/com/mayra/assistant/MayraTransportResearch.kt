package com.mayra.assistant
object MayraTransportResearch {
 enum class Type { TRAIN, BUS, FLIGHT, METRO, FERRY }
 data class Result(val type:Type,val query:String,val readOnly:Boolean=true)
 fun research(type:Type,query:String)=Result(type,query.trim())
 fun rule()="Schedules, fares and ticket availability are read-only research; ticket purchase/payment is blocked."
}