package com.mayra.assistant

object MayraPersonCaseVerification {
    data class PersonIdentity(
        val fullName:String,
        val dateOfBirth:String? = null,
        val address:String? = null,
        val identityReference:String? = null
    )
    data class PublicCase(
        val caseId:String?,
        val firNumber:String?,
        val policeStation:String?,
        val cyberCrime:Boolean,
        val court:String?,
        val status:String?,
        val judgment:String?,
        val sourceUrl:String,
        val publishedAt:String?
    )
    data class Result(
        val verifiedMatches:List<PublicCase>,
        val possibleMatches:List<PublicCase>,
        val identityConfidence:String,
        val certificateStatus:String
    )

    fun assess(person:PersonIdentity, records:List<PublicCase>):Result {
        val valid=records.filter{it.sourceUrl.isNotBlank()}
        val exact=valid.filter{it.caseId?.isNotBlank()==true}
        return Result(
            verifiedMatches=exact,
            possibleMatches=valid.filterNot{exact.contains(it)},
            identityConfidence=if(person.fullName.isBlank()) "INSUFFICIENT_IDENTITY" else "REQUIRES_OFFICIAL_IDENTITY_VERIFICATION",
            certificateStatus="PCC_MUST_BE_CONFIRMED_BY_AUTHORIZED_WEST_BENGAL_POLICE_PROCESS"
        )
    }

    fun rule() = "Use only lawfully accessible public police/CCTNS and court records. Match identity conservatively using provided identifiers; never claim a person is accused, guilty, convicted, or cleared solely from a name match. Show FIR/case, police station, cyber-crime flag, court, status, orders/judgment and source when publicly available. PCC/character clearance remains an official police-issued determination."
}
