package com.mayra.assistant
object MayraBengaliHinduCalendar {
 data class Entry(val dateLabel:String,val occasion:String,val details:String)
 fun entry(dateLabel:String,occasion:String,details:String)=Entry(dateLabel.trim(),occasion.trim(),details.trim())
 fun rule()="Provide Bengali Hindu calendar and Puja knowledge with date/location caveats; verify time-sensitive tithi details from reliable sources."
}