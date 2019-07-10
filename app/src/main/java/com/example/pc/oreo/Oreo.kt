package com.example.pc.oreo

class Oreo : WifiConnector.OreoCommandListener {

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
        wifiConnector.oreo = this
    }

    fun disconnect() {
        wifiConnector.disconnect()
    }

    override fun onNotifyCommandUpdate(): ByteArray {
        return controlCommand.toDataPacket()
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
}
