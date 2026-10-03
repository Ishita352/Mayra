package com.mayra.assistant

/**
 * Weather and destination-planning contract.
 *
 * For India, official IMD/Mausam information is preferred when available.
 * Route and traffic facts must come from verified routing or road-authority
 * sources. Forecasts and road conditions are never presented as certain.
 */
object WeatherAndTravelPolicy {
    const val DEFAULT_FORECAST_DAYS = 10
    const val DEFAULT_ROUTE_ALTERNATIVES = 3

    enum class WeatherSource { IMD, VERIFIED_WEATHER_PROVIDER, UNKNOWN }
    enum class RouteSource { VERIFIED_ROUTING_PROVIDER, OFFICIAL_ROAD_AUTHORITY, UNKNOWN }

    enum class TripDetail {
        DESTINATION_OVERVIEW, WEATHER_FORECAST, WEATHER_ALERT, RAIN_RISK,
        TEMPERATURE, WIND, VISIBILITY, TRAFFIC, ROAD_CLOSURE, TRAVEL_TIME,
        ALTERNATIVE_ROUTE, SHORTER_ROUTE, TOLL_ESTIMATE, DISTANCE, WAYPOINTS,
        LOCAL_EMERGENCY_SERVICES, LOCAL_NEWS_ALERTS, DEPARTURE_RECOMMENDATION
    }

    fun preferredWeatherSource(countryCode: String): WeatherSource =
        if (countryCode.equals("IN", ignoreCase = true)) WeatherSource.IMD
        else WeatherSource.VERIFIED_WEATHER_PROVIDER

    fun requiresVerifiedWeather(): Boolean = true
    fun requiresVerifiedRouteData(): Boolean = true
    fun mayInventForecast(): Boolean = false
    fun mayInventRoadCondition(): Boolean = false
    fun mayInventShortcut(): Boolean = false
    fun mayPresentForecastAsCertain(): Boolean = false
    fun mayNotifyOwnerOfRelevantWeatherAlert(): Boolean = true
    fun forecastDays(): Int = DEFAULT_FORECAST_DAYS
    fun maxAlternativeRoutes(): Int = DEFAULT_ROUTE_ALTERNATIVES
}