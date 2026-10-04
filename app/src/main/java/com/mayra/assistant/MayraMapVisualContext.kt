package com.mayra.assistant
object MayraMapVisualContext {
 data class Context(val place:String,val latitude:Double?,val longitude:Double?,val satelliteAllowed:Boolean)
 fun create(place:String,lat:Double?=null,lon:Double?=null,satelliteAllowed:Boolean=true)=Context(place.trim(),lat,lon,satelliteAllowed)
 fun rule()="Map/satellite context is informational; precise location is used only when explicitly provided or permitted."
}