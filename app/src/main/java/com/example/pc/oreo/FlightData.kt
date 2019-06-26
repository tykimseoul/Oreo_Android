package com.example.pc.oreo

import android.util.Log

class FlightData {
    var flyMode: Int = 0
    var altitude: Double = 0.0
    var verticalSpeed: Int = 0
    var eastSpeed: Double = 0.0
        get() {
            return speed * Math.sin(groundCourse.toDouble())
        }
    var northSpeed: Double = 0.0
        get() {
            return speed * Math.cos(groundCourse.toDouble())
        }
    var flyTime: Int = 0
    var speedMode: Int = 0

    var gpsFixed: Boolean = false
    var satelliteCount: Int = 0
    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var gpsAltitude: Int = 0
    var speed: Double = 0.0
    var groundCourse: Float = 0.0f
    var groundCourseCardinal: Int = 0
        get() {
            return if (groundCourse in 337.5..360.0 || groundCourse in 0.0..22.5) {
                0
            } else if (groundCourse in 22.5..67.5) {
                1
            } else if (groundCourse in 67.5..112.5) {
                2
            } else if (groundCourse in 112.5..157.5) {
                3
            } else if (groundCourse in 157.5..202.5) {
                4
            } else if (groundCourse in 202.5..247.5) {
                5
            } else if (groundCourse in 247.5..292.5) {
                6
            } else if (groundCourse in 292.5..337.5) {
                7
            } else {
                0
            }
        }
    var distance: Int = 0
    var homeDirection: Int = 0

    var flying: Boolean = false

    var batteryVoltage: Double = 0.0
    var batteryAmperage: Int = 0
    var batteryAdequate: Boolean = false
        get() {
            return batteryVoltage > 7
        }
    var droneFlyTimeLeft: Int = 0

    var cameraState: Int = 0
    var gravityState: Boolean = false
    var imuCalibrationState: Int = 0
    var imuState: Boolean = false
    var wifiDisturb: Int = 0
    var wifiStrength: Int = 0
    var wifiConnected: Boolean = false
    var bluetoothConnected: Boolean = false

    fun setData(payload: IntArray) {
        var ints = ""
        for (b in payload) {
            ints += String.format(" %d", b)
        }
        Log.e("ints", ints)
        var index = 0
        altitude = payload[index] / 100.0
        verticalSpeed = payload[index + 1]

        index += 2
        gpsFixed = payload[index] == 1
        satelliteCount = payload[index + 1]
        latitude = payload[index + 2] / 10000000.0
        longitude = payload[index + 3] / 10000000.0
        gpsAltitude = payload[index + 4]
        speed = payload[index + 5] / 100.0
        groundCourse = payload[index + 6] / 10.0f

        index += 7
        batteryVoltage = payload[index] / 10.0
        batteryAmperage = payload[index + 3]

        index += 4
        distance = payload[index]
        homeDirection = payload[index + 1]
    }
}
