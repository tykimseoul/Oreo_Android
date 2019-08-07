package com.example.pc.oreo

import com.beust.klaxon.Json

data class DriveData(
        @Json(name = "power")
        var power: Int = 0,
        @Json(name = "voltage")
        var voltage: Double = 0.0
) {
    @Json(ignored = true)
    val speed: Double
        //TODO: find real formula
        get() = power * voltage * 1400
    @Json(ignored = true)
    var gpsFixed: Boolean = false
    @Json(ignored = true)
    var satelliteCount: Int = 0
    @Json(ignored = true)
    var longitude: Double = 0.0
    @Json(ignored = true)
    var latitude: Double = 0.0
    @Json(ignored = true)
    var groundCourse: Float = 0.0f
    @Json(ignored = true)
    var distance: Int = 0
    @Json(ignored = true)
    var homeDirection: Int = 0

    @Json(ignored = true)
    val batteryAdequate: Boolean
        get() {
            return voltage > 7
        }
    @Json(ignored = true)
    var wifiConnected: Boolean = false
}
