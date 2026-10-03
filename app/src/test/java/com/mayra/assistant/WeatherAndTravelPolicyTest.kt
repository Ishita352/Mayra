package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherAndTravelPolicyTest {
    @Test
    fun indiaPrefersOfficialWeatherSource() {
        assertEquals(WeatherAndTravelPolicy.WeatherSource.IMD, WeatherAndTravelPolicy.preferredWeatherSource("IN"))
        assertEquals(10, WeatherAndTravelPolicy.forecastDays())
    }

    @Test
    fun travelFactsMustBeVerifiedAndUncertaintyPreserved() {
        assertTrue(WeatherAndTravelPolicy.requiresVerifiedWeather())
        assertTrue(WeatherAndTravelPolicy.requiresVerifiedRouteData())
        assertTrue(WeatherAndTravelPolicy.mayNotifyOwnerOfRelevantWeatherAlert())
        assertFalse(WeatherAndTravelPolicy.mayInventForecast())
        assertFalse(WeatherAndTravelPolicy.mayInventRoadCondition())
        assertFalse(WeatherAndTravelPolicy.mayInventShortcut())
        assertFalse(WeatherAndTravelPolicy.mayPresentForecastAsCertain())
    }
}