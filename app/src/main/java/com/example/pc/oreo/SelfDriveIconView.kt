package com.example.pc.oreo

import android.content.Context
import android.util.AttributeSet
import android.view.View

class SelfDriveIconView : StatusIconView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onClick(v: View) {
        iconClickListener.onIconClick(StatusIconType.SELF_DRIVE)
    }

    override fun updateView(value: Int) {
            setImageResource(statuses[StatusIconType.SELF_DRIVE.index][value])
    }

    companion object {
        const val SELF_DRIVE_ACTIVATED = 1
        const val SELF_DRIVE_DISABLED = 0
    }
}
