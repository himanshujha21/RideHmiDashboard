package com.example.ridehmidashboard.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Sends a new [VehicleState] every [intervalMillis]. Each scenario is repeated
 * [samplesPerScenario] times so it stays on screen long enough to read.
 */
class SimulatedVehicleDataSource(
    private val intervalMillis: Long = 1_000L,
    private val samplesPerScenario: Int = 3,
    includeFaultDemo: Boolean = true
) : VehicleDataSource {

    private class Frame(val state: VehicleState, val delayAfterMillis: Long)

    private val frames: List<Frame> = buildList {
        RIDE_SEQUENCE.forEach { addAll(repeatedFrames(it)) }
        if (includeFaultDemo) {
            // Bad speed value, then missing signals, then a gap long enough to count as stale
            addAll(repeatedFrames(SENSOR_FAULT))
            add(Frame(SIGNAL_LOST, SILENT_PERIOD_MILLIS))
        }
    }

    // Kept outside the flow so that collecting again carries on from where it stopped
    private var frameIndex = 0
    private var batteryPercent = FULL_BATTERY

    override val vehicleState: Flow<VehicleState> = flow {
        while (true) {
            val frame = frames[frameIndex]
            frameIndex = (frameIndex + 1) % frames.size
            emit(withBattery(frame.state))
            delay(frame.delayAfterMillis)
        }
    }

    override fun restart() {
        frameIndex = 0
        batteryPercent = FULL_BATTERY
    }

    // Battery drops 1% for every sample where the vehicle is moving
    private fun withBattery(state: VehicleState): VehicleState {
        if (state.scenario == RideScenario.SIGNAL_LOST) return state

        if ((state.speedKmh ?: 0) > 0) {
            batteryPercent = (batteryPercent - 1).coerceAtLeast(MIN_BATTERY)
        }
        return state.copy(batteryPercent = batteryPercent)
    }

    private fun repeatedFrames(state: VehicleState): List<Frame> =
        List(samplesPerScenario) { Frame(state, intervalMillis) }

    companion object {
        private const val SILENT_PERIOD_MILLIS = 5_000L
        private const val FULL_BATTERY = 100
        private const val MIN_BATTERY = 5

        private val RIDE_SEQUENCE = listOf(
            VehicleState(RideScenario.PARKED, speedKmh = 0, isMotorOn = false, isKillSwitchActive = false),
            VehicleState(RideScenario.READY, speedKmh = 0, isMotorOn = true, isKillSwitchActive = false),
            VehicleState(RideScenario.RIDING, speedKmh = 25, isMotorOn = true, isKillSwitchActive = false),
            VehicleState(RideScenario.ACCELERATING, speedKmh = 50, isMotorOn = true, isKillSwitchActive = false),
            VehicleState(RideScenario.MOTOR_DISABLED, speedKmh = 0, isMotorOn = false, isKillSwitchActive = true)
        )

        private val SENSOR_FAULT =
            VehicleState(RideScenario.SENSOR_FAULT, speedKmh = -12, isMotorOn = true, isKillSwitchActive = false)

        private val SIGNAL_LOST =
            VehicleState(RideScenario.SIGNAL_LOST, speedKmh = null, isMotorOn = null, isKillSwitchActive = null)
    }
}
