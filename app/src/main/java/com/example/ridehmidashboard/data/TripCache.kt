package com.example.ridehmidashboard.data

import android.content.Context
import androidx.core.content.edit

/** Keeps trip values between app launches. */
interface TripCache {
    fun load(): TripInfo
    fun save(trip: TripInfo)
}

class SharedPreferencesTripCache(context: Context) : TripCache {

    private val prefs = context.getSharedPreferences("trip_cache", Context.MODE_PRIVATE)

    override fun load(): TripInfo = TripInfo(
        odometerKm = prefs.getFloat(KEY_ODOMETER, 0f).toDouble(),
        tripKm = prefs.getFloat(KEY_TRIP, 0f).toDouble(),
        topSpeedKmh = prefs.getInt(KEY_TOP_SPEED, 0)
    )

    override fun save(trip: TripInfo) {
        prefs.edit {
            putFloat(KEY_ODOMETER, trip.odometerKm.toFloat())
            putFloat(KEY_TRIP, trip.tripKm.toFloat())
            putInt(KEY_TOP_SPEED, trip.topSpeedKmh)
        }
    }

    private companion object {
        const val KEY_ODOMETER = "odometer_km"
        const val KEY_TRIP = "trip_km"
        const val KEY_TOP_SPEED = "top_speed_kmh"
    }
}
