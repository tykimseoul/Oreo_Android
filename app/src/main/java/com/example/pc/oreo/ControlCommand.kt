package com.example.pc.oreo

import android.os.Parcel
import android.os.Parcelable

class ControlCommand : Parcelable {
    var rightX: Int = 0
        set(value) {
            field = value
            valid = 1
        }
    var rightY: Int = 0
        set(value) {
            field = value
            valid = 1
        }
    var leftX: Int = 0
        set(value) {
            field = value
            valid = 1
        }
    var leftY: Int = 0
        set(value) {
            field = value
            valid = 1
        }
    private var valid = 0

    val isValid: Boolean
        get() = valid == 1

    constructor()

    constructor(parcel: Parcel) {
        rightX = parcel.readInt()
        rightY = parcel.readInt()
        leftX = parcel.readInt()
        leftY = parcel.readInt()
        valid = parcel.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(rightX)
        dest.writeInt(rightY)
        dest.writeInt(leftX)
        dest.writeInt(leftY)
        dest.writeInt(valid)
    }

    override fun toString(): String {
        return "JoystickState(rightX=$rightX, rightY=$rightY, leftX=$leftX, leftY=$leftY, valid=$valid)"
    }

    fun convertToMSP() {
        rightX = -1 * (rightX * 500.0 / 511.0).toInt() + 1500
        rightY = -1 * (rightY * 500.0 / 511.0).toInt() + 1500
        leftX = -1 * (leftX * 500.0 / 511.0).toInt() + 1500
        leftY = -1 * (leftY * 500.0 / 511.0).toInt() + 1500
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
