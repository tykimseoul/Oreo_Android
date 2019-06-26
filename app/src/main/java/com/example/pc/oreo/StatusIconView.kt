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
                    R.raw.wifi2),
            intArrayOf(R.drawable.ic_arrow_decision_auto_white_48dp,
                    R.drawable.ic_arrow_decision_auto_green_48dp))
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
        WIFI(4),
        BATTERY(3),
        SELF_DRIVE(5)
    }

    interface IconClickListener {
        fun onIconClick(type: StatusIconType)
    }
}
