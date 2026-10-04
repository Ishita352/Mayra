package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class StepTenElevenTwelveTest {
 @Test fun socialRulesRespectAuthorizationAndPlatformPolicy(){
  assertTrue(MayraSocialIntelligencePolicy.decide(MayraSocialIntelligencePolicy.Request(MayraSocialIntelligencePolicy.Platform.WHATSAPP,false,true,"read")).startsWith("BLOCKED"))
  assertTrue(MayraSocialIntelligencePolicy.decide(MayraSocialIntelligencePolicy.Request(MayraSocialIntelligencePolicy.Platform.TELEGRAM,true,false,"send")).startsWith("MANUAL_ONLY"))
  assertTrue(MayraSocialIntelligencePolicy.decide(MayraSocialIntelligencePolicy.Request(MayraSocialIntelligencePolicy.Platform.FACEBOOK,true,true,"OTP bypass")).startsWith("BLOCKED"))
 }
 @Test fun mediaMoodAnalysisIsBounded(){
  val a=MayraMediaConversationIntelligence.analyze("এটি জরুরি")
  assertEquals(MayraMediaConversationIntelligence.Mood.URGENT,a.mood);assertTrue(a.confidence in 0.0..1.0)
 }
 @Test fun characterAndVoiceAreOptionalAndSafe(){
  val c=MayraVoiceLightAndCharacter.CharacterConfig();assertTrue(MayraVoiceLightAndCharacter.validate(c));assertTrue(MayraVoiceLightAndCharacter.safetyNotice().contains("covert"))
 }
}