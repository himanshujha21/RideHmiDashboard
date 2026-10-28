package com.example.ridehmidashboard.data

/** Current time in milliseconds. Lets tests control time when working out trip distance. */
fun interface TimeProvider {
    fun nowMillis(): Long
}
