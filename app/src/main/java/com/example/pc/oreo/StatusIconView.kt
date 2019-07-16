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
                    R.drawable.ic_arrow_decision_auto_green_48dp),
            intArrayOf(R.drawable.ic_r_red_48dp,
                    R.drawable.ic_p_white_48dp,
                    R.drawable.ic_d_blue_48dp))
    lateinit var iconClickListener: IconClickListener
    var type: StatusIconType = StatusIconType.UNCLICKABLE

    constructor(context: Context) : super(context) {
        this.setOnClickListener(this)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.setOnClickListener(this)
    }

    override fun onClick(v: View) {

    }

    open fun updateView(value: Int) {
        setImageResource(statuses[type.value][value])
    }

    enum class StatusIconType constructor(val value: Int) {
        UNCLICKABLE(0),
        BATTERY(1),
        WIFI(2),
        SELF_DRIVE(3),
        GEARBOX(4)
    }

    interface IconClickListener {
        fun onIconClick(type: StatusIconType)
    }
}
