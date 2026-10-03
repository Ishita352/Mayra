package com.mayra.assistant

/**
 * Local civic-information policy.
 *
 * The owner's primary PIN is supplied through runtime configuration rather
 * than stored in source code. Live adapters must provide verified results.
 */
object LocalCivicServicesPolicy {
    const val DEFAULT_RADIUS_KM = 50

    enum class ServiceType {
        HOSPITAL, AMBULANCE, POLICE, FIRE_RESCUE, TRAFFIC, PHARMACY,
        DISASTER_RESPONSE, EMERGENCY_HELPLINE
    }

    enum class InformationType {
        LOCAL_NEWS, EMERGENCY_ALERT, ROAD_TRAFFIC, WEATHER_HAZARD, PUBLIC_SERVICE_UPDATE
    }

    fun serviceTypes(): List<ServiceType> = ServiceType.values().toList()
    fun informationTypes(): List<InformationType> = InformationType.values().toList()

    fun isInConfiguredArea(pin: String, configuredPin: String, distanceKm: Double): Boolean =
        pin == configuredPin && distanceKm in 0.0..DEFAULT_RADIUS_KM.toDouble()

    fun mayInventLocalResult(): Boolean = false
    fun requiresVerifiedSourceForAlert(): Boolean = true
    fun mayRunBackgroundRefresh(): Boolean = true
    fun primaryAreaRadiusKm(): Int = DEFAULT_RADIUS_KM
    fun mayNotifyOwnerOfVerifiedAlert(): Boolean = true
}