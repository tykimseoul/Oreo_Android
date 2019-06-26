package com.example.pc.oreo

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View

class WifiIconView : StatusIconView {
    private val strength: Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onClick(v: View) {
        updateView(WIFI_STATE_PENDING.toDouble())
        iconClickListener.onIconClick(StatusIconView.StatusIconType.WIFI)
    }

    override fun updateView(value: Double) {
        when (value.toInt()) {
            WIFI_STATE_PENDING -> {
                setAnimation(statuses[StatusIconView.StatusIconType.WIFI.value][WIFI_STATE_PENDING])
                repeatCount = ValueAnimator.INFINITE
                playAnimation()
            }
            else -> setImageResource(statuses[StatusIconView.StatusIconType.WIFI.value][value.toInt()])
        }
    }

    companion object {
        const val WIFI_STATE_CONNECTED = 2
        const val WIFI_STATE_WEAK = 1
        const val WIFI_STATE_DISCONNECTED = 0
        const val WIFI_STATE_PENDING = 3
    }
}
