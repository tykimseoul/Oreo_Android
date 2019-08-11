package com.example.pc.oreo

import com.beust.klaxon.Json
import com.example.pc.oreo.Oreo.DriveMode

data class ControlCommand(
        @Json(name = "selfDrive")
        var selfDrive: DriveMode = DriveMode.MANUAL,
        @Json(name = "steer")
        var steerPercentage: Float = 0.0f,
        @Json(name = "speed")
        var speedPercentage: Float = 0.0f) {

    fun reset() {
        steerPercentage = 0.0f
        speedPercentage = 0.0f
    }
}
