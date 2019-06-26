package com.example.pc.oreo

import android.util.Log

class DriveData {
    enum class DriveMode constructor(val value: Int) {
        MANUAL(0),
        AUTONOMOUS(1)
    }

    var driveMode: DriveMode = DriveMode.MANUAL
    var verticalSpeed: Int = 0
    var speed: Double = 0.0
    var flyTime: Int = 0

    var gpsFixed: Boolean = false
    var satelliteCount: Int = 0
    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var groundCourse: Float = 0.0f
    var distance: Int = 0
    var homeDirection: Int = 0

    var flying: Boolean = false

    var batteryVoltage: Double = 0.0
    var batteryAmperage: Int = 0
    var batteryAdequate: Boolean = false
        get() {
            return batteryVoltage > 7
        }
    var wifiConnected: Boolean = false
    var bluetoothConnected: Boolean = false

    fun setData(payload: IntArray) {
        var ints = ""
        for (b in payload) {
            ints += String.format(" %d", b)
        }
        Log.e("ints", ints)
        var index = 0
        verticalSpeed = payload[index + 1]

        index += 2
        gpsFixed = payload[index] == 1
        satelliteCount = payload[index + 1]
        latitude = payload[index + 2] / 10000000.0
        longitude = payload[index + 3] / 10000000.0
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
