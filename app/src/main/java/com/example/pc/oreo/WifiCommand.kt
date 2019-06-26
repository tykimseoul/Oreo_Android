package com.example.pc.oreo

import org.json.JSONArray
import org.json.JSONException

class WifiCommand {
    private var rawData: ByteArray? = null
    var type: WifiCommandCode? = null
        private set
    private lateinit var payload: ByteArray
    lateinit var driveDataPayload: IntArray

    constructor(rawData: ByteArray) {
        this.rawData = rawData
        parseData()
    }

    constructor(type: Int) {
        this.type = WifiCommandCode.values().first { code -> code.value.toInt() == type }
    }

    private fun parseData() {
        type = WifiCommandCode.values().first { code -> code.value == rawData?.first() }
        when (type) {
            WifiCommandCode.DRIVE_DATA -> {
                val payloadLength = rawData!!.size - 2
                payload = ByteArray(payloadLength)
                System.arraycopy(rawData, 2, payload, 0, payloadLength)
                try {
                    val arr = JSONArray(String(payload))
                    driveDataPayload = IntArray(arr.length())
                    for (i in 0 until arr.length()) {
                        driveDataPayload[i] = arr.optInt(i)
                    }
                } catch (e: JSONException) {
                    driveDataPayload = IntArray(0)
                }
            }
            else -> {
                val payloadLength = type!!.length - 2
                payload = ByteArray(payloadLength)
                System.arraycopy(rawData, 2, payload, 0, payloadLength)
            }
        }
    }

    enum class WifiCommandCode constructor(value: Int, length: Int) {
        REQUEST_CONNECTION(100, 2),
        APPROVE_CONNECTION(101, 2),
        JOYSTICK_CONTROL(102, 14),
        TAKE_OFF(103, 2),
        LAND(104, 2),
        REQUEST_STREAM(105, 2),
        FRAME_DATA(106, 2),
        DRIVE_DATA(107, 36),
        CAMERA_SETTINGS(108, 10);

        val value: Byte = value.toByte()
        val length: Byte = length.toByte()

    }
}
