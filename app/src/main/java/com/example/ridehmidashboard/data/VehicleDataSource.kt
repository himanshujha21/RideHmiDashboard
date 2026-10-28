package com.example.ridehmidashboard.data

import kotlinx.coroutines.flow.Flow

interface VehicleDataSource {
    val vehicleState: Flow<VehicleState>

    // Only the simulator can go back to the start. A real vehicle source would ignore this.
    fun restart() {}
}
