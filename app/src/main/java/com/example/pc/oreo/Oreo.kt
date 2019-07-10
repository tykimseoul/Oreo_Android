package com.example.pc.oreo

import com.example.pc.oreo.WifiCommand.WifiCommandCode

class Oreo {
    val driveData = DriveData()
    var oreoStatusChangeListener: OreoStatusChangeListener? = null
    val wifiConnector: WifiConnector = WifiConnector("192.168.123.101", 9999)
    val controlCommand: ControlCommand = ControlCommand()

    val isReady: Boolean
        get() = (driveData.wifiConnected
                && driveData.batteryAdequate)

    val isConnected: Boolean
        get() = (wifiConnector.datagramSocket != null
                && wifiConnector.datagramSocket!!.isConnected)

    fun connect() {
        wifiConnector.connect()
    }

    fun disconnect() {
        if (driveData.flying) {
            land()
        }
        wifiConnector.disconnect()
    }

    private fun createJoyPacket(rightX: Int, rightY: Int, leftX: Int, leftY: Int, speed: Float): ByteArray {
        val packet: MutableList<Byte> = mutableListOf()
        val values: List<Int> = listOf(rightX, rightY, leftX, leftY)
        packet[0] = WifiCommandCode.JOYSTICK_CONTROL.length
        packet[1] = WifiCommandCode.JOYSTICK_CONTROL.value

        values.flatMapTo(packet) { joy -> parseToByteArray(joy) }

        return packet.toByteArray()
    }

    @Throws(IllegalArgumentException::class)
    private fun parseToByteArray(value: Int): MutableList<Byte> {
        val result: MutableList<Byte> = mutableListOf()
        result[0] = if (value > 0) 0.toByte() else 1.toByte()
        val absValue = Math.abs(value)
        if (absValue in Math.pow(2.0, 16.0).toInt() * -1..Math.pow(2.0, 16.0).toInt()) {
            result[1] = (absValue shr 8 and 0xFF).toByte()
            result[2] = (absValue and 0xFF).toByte()
        } else {
            throw IllegalArgumentException()
        }
        return result
    }

    fun startControlStream() {

    }

    fun sendControl(controlCommand: ControlCommand) {
        if (!isConnected) {
            return
        }

        var boost = 0.0f

        val rx = controlCommand.rightX
        val ry = controlCommand.rightY
        val lx = controlCommand.leftX
        val ly = controlCommand.leftY

        val packet = createJoyPacket(rx, ry, lx, ly, boost)
        wifiConnector.send(packet)
    }

    fun takeOff() {
        val packet = ByteArray(WifiCommandCode.TAKE_OFF.length.toInt())
        packet[0] = WifiCommandCode.TAKE_OFF.length
        packet[1] = WifiCommandCode.TAKE_OFF.value
        wifiConnector.send(packet)
    }

    fun land() {
        val packet = ByteArray(WifiCommandCode.LAND.length.toInt())
        packet[0] = WifiCommandCode.TAKE_OFF.length
        packet[1] = WifiCommandCode.TAKE_OFF.value
        wifiConnector.send(packet)
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
}
