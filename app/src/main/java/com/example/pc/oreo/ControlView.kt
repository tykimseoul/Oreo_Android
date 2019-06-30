package com.example.pc.oreo

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import com.curiouscreature.kotlin.math.Float2

class ControlView: View {
    lateinit var screenSize:Float2
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun consumeTouch(touchPoint:Float2):Boolean{
        return true
    }
}