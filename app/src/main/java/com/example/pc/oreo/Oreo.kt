package com.example.pc.oreo

import android.util.Log
import com.beust.klaxon.Klaxon
import kotlin.text.Charsets.UTF_8

class Oreo : WifiConnector.OreoCommandListener {

    val driveData = DriveData()
    val wifiConnector: WifiConnector = WifiConnector("192.168.123.101", 9999)
    val controlCommand: ControlCommand = ControlCommand()
    val gear = Gear()

    val isConnected: Boolean
        get() = (wifiConnector.datagramSocket != null
                && wifiConnector.datagramSocket!!.isConnected)

    fun connect() {
        wifiConnector.connect()
        wifiConnector.oreo = this
    }

    fun disconnect() {
        wifiConnector.disconnect()
    }

    override fun onNotifyCommandUpdate(): ByteArray {
        Log.e("json", Klaxon().toJsonString(controlCommand))
        return Klaxon().toJsonString(controlCommand).toByteArray(UTF_8)
    }

    fun startControlStream() {
        wifiConnector.streamControls()
    }

    fun setWifiConnection(connection: Boolean) {
        driveData.wifiConnected = connection
    }

    class Gear(var type: GearType = GearType.PARK) {
        fun shift(): GearType {
            return when (type) {
                GearType.PARK -> {
                    type = GearType.DRIVE
                    GearType.DRIVE
                }
                GearType.REVERSE -> {
                    type = GearType.PARK
                    GearType.PARK
                }
                GearType.DRIVE -> {
                    type = GearType.REVERSE
                    GearType.REVERSE
                }
            }
        }
    }

    enum class GearType constructor(val value: Int, val index: Int) {
        PARK(0, 1),
        DRIVE(1, 2),
        REVERSE(-1, 0)
    }

    enum class DriveMode constructor(val value: Int) {
        MANUAL(0),
        TRAIN(1),
        AUTONOMOUS(2);

        companion object {
            fun shiftFrom(mode: DriveMode): DriveMode {
                return values()[(mode.value + 1) % values().size]
            }
        }
    }
}
