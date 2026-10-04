package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class MayraWestBengalPublicCaseIntelligenceTest {
 @Test fun officialPublicCaseUpdateTracked(){val s=MayraWestBengalPublicCaseIntelligence.PublicSource("Official Court","https://example.gov","COURT".let{MayraWestBengalPublicCaseIntelligence.SourceType.COURT},true);val e=MayraWestBengalPublicCaseIntelligence.CaseEvent("CASE-1",MayraWestBengalPublicCaseIntelligence.EventType.CASE_UPDATE,1,"Public order",s);assertEquals("PUBLIC_UPDATE_TRACKED",MayraWestBengalPublicCaseIntelligence.track(e))}
 @Test fun unofficialOrIncompleteDataRejected(){val s=MayraWestBengalPublicCaseIntelligence.PublicSource("Unknown","https://example.com",MayraWestBengalPublicCaseIntelligence.SourceType.POLICE,false);val e=MayraWestBengalPublicCaseIntelligence.CaseEvent("","NEW_CASE",1,"x",s);assertEquals("REJECTED_UNVERIFIED",MayraWestBengalPublicCaseIntelligence.track(e))}
}