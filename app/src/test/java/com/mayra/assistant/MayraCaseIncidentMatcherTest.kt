package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class MayraCaseIncidentMatcherTest {
 @Test fun incidentCanRankPublicMatches(){val c=MayraCaseIncidentMatcher.Candidate("FIR-1","PS-A","Court-A","Pending","https://gov",85,"official update");val a=MayraCaseIncidentMatcher.match(MayraCaseIncidentMatcher.Incident("incident","Kolkata"),listOf(c));assertTrue(a.likelyPublicCaseExists);assertEquals("PS-A",a.candidates.first().policeStation)}
 @Test fun invalidSourcesRejected(){val c=MayraCaseIncidentMatcher.Candidate("x","PS","C","Pending","",101,"x");assertFalse(MayraCaseIncidentMatcher.match(MayraCaseIncidentMatcher.Incident("x"),listOf(c)).likelyPublicCaseExists)}
}