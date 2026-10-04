package com.mayra.assistant

/**
 * Device-aware multitasking policy. Mayra scales concurrency to device health
 * instead of using a fixed task-count limit.
 */
object MayraDeviceAwareMultitasking {
    enum class Health { EXCELLENT, GOOD, CAUTION, CRITICAL }
    enum class Load { LIGHT, MODERATE, HEAVY, EXCESSIVE }
    data class Capacity(
        val health: Health,
        val load: Load,
        val recommendedConcurrentTasks: Int,
        val reason: String
    )

    fun capacity(
        health: Health,
        load: Load,
        cpuAvailablePercent: Int,
        memoryAvailablePercent: Int,
        batteryPercent: Int,
        thermalHealthy: Boolean
    ): Capacity {
        if (!thermalHealthy || health == Health.CRITICAL || cpuAvailablePercent < 10 || memoryAvailablePercent < 10)
            return Capacity(Health.CRITICAL, Load.EXCESSIVE, 1, "Protect the device: reduce workload and avoid risky changes.")
        val base = when (health) {
            Health.EXCELLENT -> 8
            Health.GOOD -> 6
            Health.CAUTION -> 3
            Health.CRITICAL -> 1
        }
        val loadFactor = when (load) {
            Load.LIGHT -> 1.0
            Load.MODERATE -> 0.8
            Load.HEAVY -> 0.5
            Load.EXCESSIVE -> 0.25
        }
        val resourceFactor = when {
            cpuAvailablePercent >= 60 && memoryAvailablePercent >= 60 && batteryPercent >= 50 -> 1.0
            cpuAvailablePercent >= 35 && memoryAvailablePercent >= 35 && batteryPercent >= 25 -> 0.75
            else -> 0.5
        }
        val recommended = (base.toDouble() * loadFactor * resourceFactor).toInt().coerceIn(1, base)
        return Capacity(health, load, recommended, "Concurrency is adapted to current device health and available resources.")
    }

    fun ownerPermissionForTemporaryLoadIncrease(
        requestedExtraTasks: Int,
        ownerApproved: Boolean,
        thermalHealthy: Boolean,
        batteryHealthy: Boolean,
        storageHealthy: Boolean
    ): Boolean =
        requestedExtraTasks > 0 && ownerApproved && thermalHealthy && batteryHealthy && storageHealthy

    fun protectionRule() =
        "Mayra must prefer device health over maximum concurrency. It should reduce, pause or queue work when CPU, memory, battery, storage or thermal conditions become unsafe."

    fun temporaryIncreaseRule() =
        "If workload is high, Mayra may request Owner permission for a temporary, modest workload increase. Even after approval it must remain inside safe thermal, battery, storage and OS limits; it may not disable protection, security or health safeguards."

    fun neverDoRule() =
        "Never overclock, bypass thermal protection, disable battery/OS safeguards, intentionally overheat hardware, or continue heavy work when health is unsafe."
}
