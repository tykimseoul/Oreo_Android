package com.example.pc.oreo

import android.content.Context
import android.util.AttributeSet
import android.view.View

class BatteryIconView : StatusIconView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun updateView(value: Int) {
        when {
            value > 10 -> setImageResource(statuses[StatusIconType.BATTERY.index][3])
            value > 7 -> setImageResource(statuses[StatusIconType.BATTERY.index][2])
            value > 3.5 -> setImageResource(statuses[StatusIconType.BATTERY.index][1])
            else -> setImageResource(statuses[StatusIconType.BATTERY.index][0])
        }
    }
    override fun onClick(v: View) {
        return
    }
}
