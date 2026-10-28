package com.example.ridehmidashboard.data

/**
 * Raw signals from the vehicle. A null value means the signal is missing,
 * which can happen with a real bus connection.
 */
data class VehicleState(
    val scenario: RideScenario,
    val speedKmh: Int?,
    val isMotorOn: Boolean?,
    val isKillSwitchActive: Boolean?,
    val batteryPercent: Int? = null
)

enum class RideScenario {
    PARKED,
    READY,
    RIDING,
    ACCELERATING,
    MOTOR_DISABLED,
    SENSOR_FAULT,
    SIGNAL_LOST
}
