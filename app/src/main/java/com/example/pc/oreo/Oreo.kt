package com.example.pc.oreo

import android.util.Log
import com.beust.klaxon.Klaxon
import kotlin.text.Charsets.UTF_8

class Oreo : WifiConnector.OreoCommandListener {

    val driveData = DriveData()
    var oreoStatusChangeListener: OreoStatusChangeListener? = null
    val wifiConnector: WifiConnector = WifiConnector("192.168.123.101", 9999)
    val controlCommand: ControlCommand = ControlCommand()
    val gear = Gear()

    val isReady: Boolean
        get() = (driveData.wifiConnected
                && driveData.batteryAdequate)

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
        oreoStatusChangeListener?.onConnectionChanged()
    }

    fun updateDriveData(data: IntArray) {
        driveData.setData(data)
        oreoStatusChangeListener?.onBatteryStateUpdate(driveData.batteryVoltage, driveData.batteryAmperage)
        oreoStatusChangeListener?.onMovementUpdate(driveData.speed)
    }

    interface OreoStatusChangeListener {
        fun onConnectionChanged()

        fun onBatteryStateUpdate(voltage: Double, amperage: Int)

        fun onMovementUpdate(velocity: Double)
    }

    class Gear(var type: GearType = GearType.PARK) {
        val next: GearType by lazy { shift() }

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
}
