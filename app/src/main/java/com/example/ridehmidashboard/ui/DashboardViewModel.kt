package com.example.ridehmidashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridehmidashboard.data.TimeProvider
import com.example.ridehmidashboard.data.TripCache
import com.example.ridehmidashboard.data.TripInfo
import com.example.ridehmidashboard.data.VehicleDataSource
import com.example.ridehmidashboard.data.VehicleState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dataSource: VehicleDataSource,
    private val tripCache: TripCache,
    private val timeProvider: TimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(trip = tripCache.load()))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null
    private var staleTimerJob: Job? = null
    private var lastSampleMillis: Long? = null

    init {
        startCollecting()
    }

    fun pause() {
        if (_uiState.value.isPaused) return
        stopCollecting()
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resume() {
        if (!_uiState.value.isPaused) return
        _uiState.update { it.copy(isPaused = false) }
        startCollecting()
    }

    /** Starts the ride again from the first scenario and clears the trip. */
    fun restart() {
        stopCollecting()
        dataSource.restart()

        val trip = _uiState.value.trip.copy(tripKm = 0.0, topSpeedKmh = 0)
        tripCache.save(trip)
        _uiState.value = DashboardUiState(trip = trip)

        startCollecting()
    }

    private fun startCollecting() {
        lastSampleMillis = null
        // Started before the first sample so that "no data at all" is also caught
        restartStaleTimer()
        collectJob = viewModelScope.launch {
            dataSource.vehicleState
                .catch { onDataSourceError() }
                .collect { state -> onNewState(state) }
        }
    }

    private fun stopCollecting() {
        collectJob?.cancel()
        staleTimerJob?.cancel()
    }

    private fun onNewState(state: VehicleState) {
        val newState = mapToUiState(state)
        val trip = updateTrip(newState.speedKmh)
        _uiState.value = newState.copy(trip = trip)
        restartStaleTimer()
    }

    private fun restartStaleTimer() {
        staleTimerJob?.cancel()
        staleTimerJob = viewModelScope.launch {
            delay(STALE_TIMEOUT_MILLIS)
            showNoData(DashboardWarning.NO_DATA)
        }
    }

    private fun onDataSourceError() {
        staleTimerJob?.cancel()
        showNoData(DashboardWarning.SOURCE_ERROR)
    }

    // Clear the old values so an out-of-date speed is never shown as current
    private fun showNoData(warning: DashboardWarning) {
        _uiState.update {
            it.copy(
                speedKmh = null,
                motorStatus = MotorStatus.UNKNOWN,
                killSwitchStatus = KillSwitchStatus.UNKNOWN,
                warning = warning,
                isStale = true,
                isOverSpeed = false,
                batteryPercent = null,
                rangeKm = null
            )
        }
    }

    private fun updateTrip(speedKmh: Int?): TripInfo {
        val now = timeProvider.nowMillis()
        // Cap the gap so a long pause in data doesn't add a big jump in distance
        val elapsedMillis = lastSampleMillis?.let { (now - it).coerceAtMost(MAX_SAMPLE_GAP_MILLIS) } ?: 0L
        lastSampleMillis = now

        val current = _uiState.value.trip
        if (speedKmh == null) return current

        val distanceKm = speedKmh * elapsedMillis / MILLIS_PER_HOUR
        val trip = current.copy(
            odometerKm = current.odometerKm + distanceKm,
            tripKm = current.tripKm + distanceKm,
            topSpeedKmh = maxOf(current.topSpeedKmh, speedKmh)
        )
        if (trip != current) {
            tripCache.save(trip)
        }
        return trip
    }

    private fun mapToUiState(state: VehicleState): DashboardUiState {
        val validSpeed = state.speedKmh?.takeIf { it in 0..MAX_VALID_SPEED_KMH }
        val validBattery = state.batteryPercent?.takeIf { it in 0..100 }
        val hasMissingSignal = state.speedKmh == null ||
            state.isMotorOn == null ||
            state.isKillSwitchActive == null

        val warning = when {
            state.speedKmh != null && validSpeed == null -> DashboardWarning.INVALID_SPEED
            hasMissingSignal -> DashboardWarning.MISSING_SIGNAL
            else -> null
        }

        return DashboardUiState(
            scenario = state.scenario,
            speedKmh = validSpeed,
            motorStatus = toMotorStatus(state.isMotorOn),
            killSwitchStatus = toKillSwitchStatus(state.isKillSwitchActive),
            warning = warning,
            isOverSpeed = validSpeed != null && validSpeed > SPEED_LIMIT_KMH,
            batteryPercent = validBattery,
            rangeKm = validBattery?.let { (it * RANGE_KM_PER_PERCENT).toInt() }
        )
    }

    private fun toMotorStatus(isMotorOn: Boolean?): MotorStatus = when (isMotorOn) {
        true -> MotorStatus.ON
        false -> MotorStatus.OFF
        null -> MotorStatus.UNKNOWN
    }

    private fun toKillSwitchStatus(isActive: Boolean?): KillSwitchStatus = when (isActive) {
        true -> KillSwitchStatus.ACTIVE
        false -> KillSwitchStatus.INACTIVE
        null -> KillSwitchStatus.UNKNOWN
    }

    companion object {
        const val STALE_TIMEOUT_MILLIS = 3_000L
        const val MAX_VALID_SPEED_KMH = 150
        const val SPEED_LIMIT_KMH = 45
        private const val MAX_SAMPLE_GAP_MILLIS = 2_000L
        private const val MILLIS_PER_HOUR = 3_600_000.0

        // Assumes about 120 km on a full charge
        const val RANGE_KM_PER_PERCENT = 1.2
    }
}
