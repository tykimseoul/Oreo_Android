package com.example.pc.oreo

import android.content.Context
import android.util.AttributeSet
import android.view.View

class GearboxIconView : StatusIconView {
    private val gear: Oreo.Gear = Oreo.Gear()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onClick(v: View) {
        iconClickListener.onIconClick(StatusIconType.GEARBOX)
        gear.shift()
        updateView(gear.type.value + 1)
    }

    override fun updateView(value: Int) {
        setImageResource(statuses[StatusIconType.GEARBOX.value][value])
    }
}