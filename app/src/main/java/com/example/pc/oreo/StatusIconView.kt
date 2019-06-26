package com.example.pc.oreo

import android.content.Context
import android.util.AttributeSet
import android.view.View

import com.airbnb.lottie.LottieAnimationView

open class StatusIconView : LottieAnimationView, View.OnClickListener {
    val statuses = arrayOf(intArrayOf(R.drawable.ic_speedometer_white_48dp),
            intArrayOf(R.drawable.ic_battery_20_white_48dp,
                    R.drawable.ic_battery_50_white_48dp,
                    R.drawable.ic_battery_80_white_48dp,
                    R.drawable.ic_battery_full_white_48dp),
            intArrayOf(R.drawable.ic_wifi_off_white_48dp,
                    R.drawable.ic_wifi_weak_white_48dp,
                    R.drawable.ic_wifi_white_48dp,
                    R.raw.wifi2))
    lateinit var iconClickListener: IconClickListener
    var type = -1

    constructor(context: Context) : super(context) {
        this.setOnClickListener(this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.setOnClickListener(this)
    }

    override fun onClick(v: View) {

    }

    open fun updateView(value: Double) {
        setImageResource(statuses[type][value.toInt()])
    }

    enum class StatusIconType constructor(val value: Int) {
        UNCLICKABLE(0),
        TIMER(1),
        WIFI(4),
        BLUETOOTH(5),
        BATTERY(3),
        GPS(6)
    }

    interface IconClickListener {
        fun onIconClick(type: StatusIconType)
    }
}
