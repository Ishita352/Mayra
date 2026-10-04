package com.mayra.assistant
object MayraStayFoodResearch {
 enum class Category { ACCOMMODATION, FOOD }
 data class Result(val category:Category,val query:String,val readOnly:Boolean=true)
 fun research(category:Category,query:String)=Result(category,query.trim())
 fun rule()="Prices and availability are research only; reservations, purchases and payments require explicit Owner control and financial actions remain blocked."
}