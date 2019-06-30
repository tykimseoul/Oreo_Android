package com.example.pc.oreo

import android.content.Context
import android.util.AttributeSet
import android.view.View

class SelfDriveIconView : StatusIconView {
    var selfDrive:Boolean=false
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onClick(v: View) {
        updateView(0.0)
        iconClickListener.onIconClick(StatusIconType.SELF_DRIVE)
    }

    override fun updateView(value: Double) {
        if(selfDrive)
            setImageResource(statuses[StatusIconType.SELF_DRIVE.value][SELF_DRIVE_DISABLED])
        else
            setImageResource(statuses[StatusIconType.SELF_DRIVE.value][SELF_DRIVE_ACTIVATED])
        selfDrive = !selfDrive
    }

    companion object {
        const val SELF_DRIVE_ACTIVATED = 1
        const val SELF_DRIVE_DISABLED = 0
    }
}
