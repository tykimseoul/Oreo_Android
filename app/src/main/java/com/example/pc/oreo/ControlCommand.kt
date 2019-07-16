package com.example.pc.oreo

import android.os.Parcel
import android.os.Parcelable
import com.beust.klaxon.Json

data class ControlCommand(
        @Json(name = "steer")
        var steerPercentage: Float = 0.0f,
        @Json(name = "speed")
        var speedPercentage: Float = 0.0f) : Parcelable {

    constructor(parcel: Parcel) : this() {
        steerPercentage = parcel.readFloat()
        speedPercentage = parcel.readFloat()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(steerPercentage)
        parcel.writeFloat(speedPercentage)
    }

    fun reset() {
        steerPercentage = 0.0f
        speedPercentage = 0.0f
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ControlCommand> {
        override fun createFromParcel(parcel: Parcel): ControlCommand {
            return ControlCommand(parcel)
        }

        override fun newArray(size: Int): Array<ControlCommand?> {
            return arrayOfNulls(size)
        }
    }
}
