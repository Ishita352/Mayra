package com.mayra.assistant
import org.junit.Assert.*
import org.junit.Test
class StepThirteenSeventeenTest {
 @Test fun localServicesUseOfficialAndSafeRules(){assertTrue(MayraLocalServicesWorkflow.plan(MayraLocalServicesWorkflow.Request(MayraLocalServicesWorkflow.Kind.GOVERNMENT,"form",null)).contains("official"))}
 @Test fun travelPlanIsBuilt(){assertEquals("Kolkata",MayraTravelPlanner.build("Kolkata","Delhi",listOf("museum")).origin)}
 @Test fun transportIsReadOnly(){assertTrue(MayraTransportResearch.research(MayraTransportResearch.Type.TRAIN,"123").readOnly)}
 @Test fun stayFoodIsReadOnly(){assertTrue(MayraStayFoodResearch.research(MayraStayFoodResearch.Category.FOOD,"near me").readOnly)}
 @Test fun calendarEntryPersistsValues(){assertEquals("Durga Puja",MayraBengaliHinduCalendar.entry("date","Durga Puja","details").occasion)}
}