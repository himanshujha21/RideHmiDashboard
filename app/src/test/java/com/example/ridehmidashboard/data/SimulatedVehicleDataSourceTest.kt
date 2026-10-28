package com.example.ridehmidashboard.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatedVehicleDataSourceTest {

    @Test
    fun emitsAllFiveScenariosInOrder() = runTest {
        val dataSource = SimulatedVehicleDataSource(samplesPerScenario = 1)

        val states = dataSource.vehicleState.take(5).toList()

        assertEquals(
            listOf(
                RideScenario.PARKED,
                RideScenario.READY,
                RideScenario.RIDING,
                RideScenario.ACCELERATING,
                RideScenario.MOTOR_DISABLED
            ),
            states.map { it.scenario }
        )
        assertEquals(listOf(0, 0, 25, 50, 0), states.map { it.speedKmh })
        assertEquals(listOf(false, true, true, true, false), states.map { it.isMotorOn })
        assertEquals(listOf(false, false, false, false, true), states.map { it.isKillSwitchActive })
    }

    @Test
    fun emitsOncePerInterval() = runTest {
        val dataSource = SimulatedVehicleDataSource(intervalMillis = 1_000L, samplesPerScenario = 1)

        dataSource.vehicleState.take(3).toList()

        // Three emissions with a one second gap between each
        assertEquals(2_000L, testScheduler.currentTime)
    }

    @Test
    fun collectingAgain_continuesWhereItStopped() = runTest {
        val dataSource = SimulatedVehicleDataSource(samplesPerScenario = 1)

        dataSource.vehicleState.take(2).toList()
        val next = dataSource.vehicleState.first()

        assertEquals(RideScenario.RIDING, next.scenario)
    }

    @Test
    fun restart_startsFromParkedAgain() = runTest {
        val dataSource = SimulatedVehicleDataSource(samplesPerScenario = 1)
        dataSource.vehicleState.take(3).toList()

        dataSource.restart()

        assertEquals(RideScenario.PARKED, dataSource.vehicleState.first().scenario)
    }

    @Test
    fun battery_dropsOnlyWhileMoving() = runTest {
        val dataSource = SimulatedVehicleDataSource(samplesPerScenario = 1)

        val states = dataSource.vehicleState.take(5).toList()

        // Parked and Ready don't use battery, Riding and Accelerating use 1% each
        assertEquals(listOf(100, 100, 99, 98, 98), states.map { it.batteryPercent })
    }
}
