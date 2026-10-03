package com.mayra.assistant

/**
 * Nearby emergency and public-information contract.
 * The owner's area is supplied securely at runtime.
 */
object NearbyServicePolicy {
    const val DEFAULT_RADIUS_KM = 50

    enum class ServiceType {
        HOSPITAL, AMBULANCE, POLICE, FIRE_RESCUE, TRAFFIC, PHARMACY, DISASTER_RESPONSE
    }

    enum class InformationType {
        LOCAL_NEWS, EMERGENCY_ALERT, ROAD_TRAFFIC, WEATHER_HAZARD, PUBLIC_SERVICE_UPDATE
    }

    fun isWithinRadius(distanceKm: Double): Boolean =
        distanceKm in 0.0..DEFAULT_RADIUS_KM.toDouble()

    fun mayInventResult(): Boolean = false
    fun requiresVerifiedSourceForAlert(): Boolean = true
    fun mayRefreshInBackground(): Boolean = true
}
