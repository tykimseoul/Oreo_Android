package com.example.pc.oreo

import android.content.Context
import android.util.AttributeSet

class BatteryIconView : StatusIconView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun updateView(value: Double) {
        when {
            value > 10 -> setImageResource(statuses[StatusIconView.StatusIconType.BATTERY.value][3])
            value > 7 -> setImageResource(statuses[StatusIconView.StatusIconType.BATTERY.value][2])
            value > 3.5 -> setImageResource(statuses[StatusIconView.StatusIconType.BATTERY.value][1])
            else -> setImageResource(statuses[StatusIconView.StatusIconType.BATTERY.value][0])
        }
    }
}
