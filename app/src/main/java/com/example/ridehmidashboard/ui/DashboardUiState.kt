package com.example.ridehmidashboard.ui

import com.example.ridehmidashboard.data.RideScenario
import com.example.ridehmidashboard.data.TripInfo

data class DashboardUiState(
    val scenario: RideScenario? = null,
    val speedKmh: Int? = null,
    val motorStatus: MotorStatus = MotorStatus.UNKNOWN,
    val killSwitchStatus: KillSwitchStatus = KillSwitchStatus.UNKNOWN,
    val warning: DashboardWarning? = null,
    val isStale: Boolean = false,
    val isOverSpeed: Boolean = false,
    val isPaused: Boolean = false,
    val batteryPercent: Int? = null,
    val rangeKm: Int? = null,
    val trip: TripInfo = TripInfo()
)

enum class MotorStatus { ON, OFF, UNKNOWN }

enum class KillSwitchStatus { ACTIVE, INACTIVE, UNKNOWN }

enum class DashboardWarning {
    INVALID_SPEED,
    MISSING_SIGNAL,
    NO_DATA,
    SOURCE_ERROR
}
