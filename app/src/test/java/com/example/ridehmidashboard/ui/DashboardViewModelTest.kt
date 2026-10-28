package com.example.ridehmidashboard.ui

import com.example.ridehmidashboard.data.RideScenario
import com.example.ridehmidashboard.data.TimeProvider
import com.example.ridehmidashboard.data.TripCache
import com.example.ridehmidashboard.data.TripInfo
import com.example.ridehmidashboard.data.VehicleDataSource
import com.example.ridehmidashboard.data.VehicleState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val fakeDataSource = FakeVehicleDataSource()
    private val fakeTripCache = FakeTripCache()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(dataSource: VehicleDataSource = fakeDataSource) =
        DashboardViewModel(dataSource, fakeTripCache, TimeProvider { testDispatcher.scheduler.currentTime })

    @Test
    fun rideScenarios_updateUiState() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        fakeDataSource.send(VehicleState(RideScenario.PARKED, 0, isMotorOn = false, isKillSwitchActive = false))
        runCurrent()
        var state = viewModel.uiState.value
        assertEquals(RideScenario.PARKED, state.scenario)
        assertEquals(0, state.speedKmh)
        assertEquals(MotorStatus.OFF, state.motorStatus)
        assertEquals(KillSwitchStatus.INACTIVE, state.killSwitchStatus)

        fakeDataSource.send(VehicleState(RideScenario.RIDING, 25, isMotorOn = true, isKillSwitchActive = false))
        runCurrent()
        state = viewModel.uiState.value
        assertEquals(25, state.speedKmh)
        assertEquals(MotorStatus.ON, state.motorStatus)
        assertNull(state.warning)

        fakeDataSource.send(VehicleState(RideScenario.MOTOR_DISABLED, 0, isMotorOn = false, isKillSwitchActive = true))
        runCurrent()
        state = viewModel.uiState.value
        assertEquals(MotorStatus.OFF, state.motorStatus)
        assertEquals(KillSwitchStatus.ACTIVE, state.killSwitchStatus)
    }

    @Test
    fun negativeSpeed_isHiddenAndShowsWarning() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        fakeDataSource.send(VehicleState(RideScenario.SENSOR_FAULT, -12, isMotorOn = true, isKillSwitchActive = false))
        runCurrent()

        val state = viewModel.uiState.value
        assertNull(state.speedKmh)
        assertEquals(DashboardWarning.INVALID_SPEED, state.warning)
        // The other signals are still fine and should keep showing
        assertEquals(MotorStatus.ON, state.motorStatus)
    }

    @Test
    fun speedAboveLimit_isTreatedAsInvalid() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        fakeDataSource.send(VehicleState(RideScenario.RIDING, 999, isMotorOn = true, isKillSwitchActive = false))
        runCurrent()

        assertNull(viewModel.uiState.value.speedKmh)
        assertEquals(DashboardWarning.INVALID_SPEED, viewModel.uiState.value.warning)
    }

    @Test
    fun missingSignals_showAsUnknown() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        fakeDataSource.send(VehicleState(RideScenario.SIGNAL_LOST, null, isMotorOn = null, isKillSwitchActive = null))
        runCurrent()

        val state = viewModel.uiState.value
        assertNull(state.speedKmh)
        assertEquals(MotorStatus.UNKNOWN, state.motorStatus)
        assertEquals(KillSwitchStatus.UNKNOWN, state.killSwitchStatus)
        assertEquals(DashboardWarning.MISSING_SIGNAL, state.warning)
    }

    @Test
    fun noDataForTimeout_marksStateAsStale() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        fakeDataSource.send(VehicleState(RideScenario.RIDING, 25, isMotorOn = true, isKillSwitchActive = false))
        runCurrent()
        assertFalse(viewModel.uiState.value.isStale)

        advanceTimeBy(DashboardViewModel.STALE_TIMEOUT_MILLIS + 1)

        val state = viewModel.uiState.value
        assertTrue(state.isStale)
        assertNull(state.speedKmh)
        assertEquals(MotorStatus.UNKNOWN, state.motorStatus)
        assertEquals(DashboardWarning.NO_DATA, state.warning)
    }

    @Test
    fun newDataAfterStale_recovers() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceTimeBy(DashboardViewModel.STALE_TIMEOUT_MILLIS + 1)
        assertTrue(viewModel.uiState.value.isStale)

        fakeDataSource.send(VehicleState(RideScenario.READY, 0, isMotorOn = true, isKillSwitchActive = false))
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isStale)
        assertEquals(MotorStatus.ON, state.motorStatus)
        assertNull(state.warning)
    }

    @Test
    fun dataSourceError_doesNotCrash() = runTest(testDispatcher) {
        val failingDataSource = object : VehicleDataSource {
            override val vehicleState: Flow<VehicleState> = flow {
                throw IllegalStateException("Bus disconnected")
            }
        }

        val viewModel = createViewModel(failingDataSource)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isStale)
        assertEquals(DashboardWarning.SOURCE_ERROR, state.warning)
    }

    @Test
    fun speedOverLimit_setsOverSpeed() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        fakeDataSource.send(VehicleState(RideScenario.ACCELERATING, 50, isMotorOn = true, isKillSwitchActive = false))
        runCurrent()
        assertTrue(viewModel.uiState.value.isOverSpeed)

        fakeDataSource.send(VehicleState(RideScenario.RIDING, 25, isMotorOn = true, isKillSwitchActive = false))
        runCurrent()
        assertFalse(viewModel.uiState.value.isOverSpeed)
    }

    @Test
    fun speedSamples_addTripDistanceAndTopSpeed() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        val riding = VehicleState(RideScenario.RIDING, 36, isMotorOn = true, isKillSwitchActive = false)

        fakeDataSource.send(riding)
        runCurrent()
        advanceTimeBy(1_000)
        fakeDataSource.send(riding)
        runCurrent()

        // 36 km/h for one second is 10 metres
        val trip = viewModel.uiState.value.trip
        assertEquals(0.01, trip.tripKm, 0.0001)
        assertEquals(0.01, trip.odometerKm, 0.0001)
        assertEquals(36, trip.topSpeedKmh)
        assertEquals(trip, fakeTripCache.savedTrip)
    }

    @Test
    fun cachedTrip_isShownOnStart() = runTest(testDispatcher) {
        fakeTripCache.savedTrip = TripInfo(odometerKm = 1324.0, tripKm = 5.5, topSpeedKmh = 60)

        val viewModel = createViewModel()

        assertEquals(1324.0, viewModel.uiState.value.trip.odometerKm, 0.0)
        assertEquals(60, viewModel.uiState.value.trip.topSpeedKmh)
    }

    @Test
    fun pause_stopsUpdatesAndStaleTimer() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        fakeDataSource.send(VehicleState(RideScenario.RIDING, 25, isMotorOn = true, isKillSwitchActive = false))
        runCurrent()

        viewModel.pause()
        fakeDataSource.send(VehicleState(RideScenario.ACCELERATING, 50, isMotorOn = true, isKillSwitchActive = false))
        advanceTimeBy(DashboardViewModel.STALE_TIMEOUT_MILLIS + 1)

        val state = viewModel.uiState.value
        assertTrue(state.isPaused)
        assertFalse(state.isStale)
        assertEquals(25, state.speedKmh)

        viewModel.resume()
        runCurrent()
        assertFalse(viewModel.uiState.value.isPaused)
        assertEquals(50, viewModel.uiState.value.speedKmh)
    }

    @Test
    fun restart_resetsTripAndDataSource() = runTest(testDispatcher) {
        fakeTripCache.savedTrip = TripInfo(odometerKm = 100.0, tripKm = 2.0, topSpeedKmh = 50)
        val viewModel = createViewModel()
        runCurrent()

        viewModel.restart()

        val trip = viewModel.uiState.value.trip
        assertEquals(1, fakeDataSource.restartCount)
        assertEquals(100.0, trip.odometerKm, 0.0)
        assertEquals(0.0, trip.tripKm, 0.0)
        assertEquals(0, trip.topSpeedKmh)
    }

    @Test
    fun battery_isShownWithRange() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        fakeDataSource.send(
            VehicleState(RideScenario.READY, 0, isMotorOn = true, isKillSwitchActive = false, batteryPercent = 50)
        )
        runCurrent()

        assertEquals(50, viewModel.uiState.value.batteryPercent)
        assertEquals(60, viewModel.uiState.value.rangeKm)
    }

    @Test
    fun invalidBattery_isHidden() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        fakeDataSource.send(
            VehicleState(RideScenario.READY, 0, isMotorOn = true, isKillSwitchActive = false, batteryPercent = 140)
        )
        runCurrent()

        assertNull(viewModel.uiState.value.batteryPercent)
        assertNull(viewModel.uiState.value.rangeKm)
    }

    private class FakeVehicleDataSource : VehicleDataSource {
        private val states = MutableSharedFlow<VehicleState>(replay = 1)
        override val vehicleState: Flow<VehicleState> = states
        var restartCount = 0

        fun send(state: VehicleState) {
            states.tryEmit(state)
        }

        override fun restart() {
            restartCount++
        }
    }

    private class FakeTripCache : TripCache {
        var savedTrip = TripInfo()

        override fun load(): TripInfo = savedTrip

        override fun save(trip: TripInfo) {
            savedTrip = trip
        }
    }
}
