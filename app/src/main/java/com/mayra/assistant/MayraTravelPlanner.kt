package com.mayra.assistant
object MayraTravelPlanner {
 data class Plan(val origin:String,val destination:String,val interests:List<String>)
 fun build(origin:String,destination:String,interests:List<String>)=Plan(origin.trim(),destination.trim(),interests.filter{it.isNotBlank()})
 fun rule()="Travel information is research/planning; bookings, payments and legally binding actions require Owner control."
}