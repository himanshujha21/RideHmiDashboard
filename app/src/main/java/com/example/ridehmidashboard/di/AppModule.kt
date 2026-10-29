package com.example.ridehmidashboard.di

import android.content.Context
import android.os.SystemClock
import com.example.ridehmidashboard.data.SharedPreferencesTripCache
import com.example.ridehmidashboard.data.SimulatedVehicleDataSource
import com.example.ridehmidashboard.data.TimeProvider
import com.example.ridehmidashboard.data.TripCache
import com.example.ridehmidashboard.data.VehicleDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Swap this for a real implementation to use live vehicle data
    @Provides
    @Singleton
    fun provideVehicleDataSource(): VehicleDataSource = SimulatedVehicleDataSource()

    @Provides
    @Singleton
    fun provideTripCache(@ApplicationContext context: Context): TripCache =
        SharedPreferencesTripCache(context)

    @Provides
    fun provideTimeProvider(): TimeProvider = TimeProvider { SystemClock.elapsedRealtime() }
}
