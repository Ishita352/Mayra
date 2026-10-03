package com.mayra.assistant

/**
 * Local civic-information configuration for the owner's primary area.
 *
 * Live maps/news adapters are separate integrations. This policy never invents
 * a nearby service or local-news result when a verified source is unavailable.
 */
object LocalCivicServicesPolicy {
    const val PRIMARY_PIN = "713405"
    const val RADIUS_KM = 50

    enum class ServiceType {
        HOSPITAL,
        AMBULANCE,
        POLICE,
        FIRE_RESCUE,
        TRAFFIC,
        PHARMACY,
        DISASTER_RESPONSE,
        EMERGENCY_HELPLINE
    }

    enum class InformationType {
        LOCAL_NEWS,
        EMERGENCY_ALERT,
        ROAD_TRAFFIC,
        WEATHER_HAZARD,
        PUBLIC_SERVICE_UPDATE
    }

    fun serviceTypes(): List<ServiceType> = ServiceType.values().toList()
    fun informationTypes(): List<InformationType> = InformationType.values().toList()

    fun isInConfiguredArea(pin: String, distanceKm: Double): Boolean =
        pin == PRIMARY_PIN && distanceKm in 0.0..RADIUS_KM.toDouble()

    fun mayInventLocalResult(): Boolean = false
    fun requiresVerifiedSourceForAlert(): Boolean = true
    fun mayRunBackgroundRefresh(): Boolean = true
    fun primaryAreaRadiusKm(): Int = DEFAULT_RADIUS_KM
    fun mayNotifyOwnerOfVerifiedAlert(): Boolean = true
}
