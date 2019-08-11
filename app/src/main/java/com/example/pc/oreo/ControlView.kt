package com.example.pc.oreo

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import com.curiouscreature.kotlin.math.Float2

class ControlView : View {
    private val screenSize: Float2 by lazy {
        val displayMetrics = DisplayMetrics()
        (context as MainActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        Float2(displayMetrics.widthPixels.toFloat(), displayMetrics.heightPixels.toFloat())
    }
    private val controlViewListener: ControlViewListener by lazy { context as MainActivity }
    private lateinit var ids: Array<Int?>
    private lateinit var types: Array<TouchType?>

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private fun consumeTouch(pair: Pair<Float2?, TouchType?>) {
        pair.first?.let {
            when (pair.second) {
                TouchType.STEER -> calculateSteerAngle(it).let { angle ->
                    controlViewListener.onSteer(angle)
                }
                TouchType.ACCELERATE -> calculateAcceleration(it).let { speed ->
                    controlViewListener.onAccelerate(speed)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                ids = arrayOf(event.getPointerIdOrElse(0), event.getPointerIdOrElse(1))
                types = ids.map { i -> getTouchType(event.getPointerCoordinates(i)) }.toTypedArray()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                ids[event.actionIndex] = event.getPointerId(event.actionIndex)
                types = ids.map { i -> getTouchType(event.getPointerCoordinates(i)) }.toTypedArray()
                if (types.size != types.distinct().size) {
                    ids[event.actionIndex] = null
                    types[event.actionIndex] = null
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                ids[event.actionIndex] = null
                types = ids.map { i -> getTouchType(event.getPointerCoordinates(i)) }.toTypedArray()
                if (!types.contains(TouchType.STEER)) {
                    controlViewListener.returnToCenter()
                }
            }
            MotionEvent.ACTION_UP -> {
                ids.fill(null)
                types.fill(null)
                controlViewListener.returnToCenter()
            }
            MotionEvent.ACTION_MOVE -> {
                val points = ids.map { i -> event.getPointerCoordinates(i) }
                (points zip types).forEach { consumeTouch(it) }
                performClick()
            }
        }
        return true
    }

    private fun MotionEvent.getPointerIdOrElse(idx: Int): Int? {
        return try {
            getPointerId(idx)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun MotionEvent.getPointerCoordinates(id: Int?): Float2? {
        return id?.let {
            findPointerIndex(it).let { pointerIndex ->
                Float2(getX(pointerIndex), getY(pointerIndex))
            }
        }
    }

    private fun getTouchType(touchPoint: Float2?): TouchType? {
        return touchPoint?.let {
            if (it.x < screenSize.x / 2) {
                TouchType.STEER
            } else {
                TouchType.ACCELERATE
            }
        }
    }

    private fun calculateSteerAngle(touchPoint: Float2): Float {
        val range = screenSize.x / 4.0
        val xDisplacement = touchPoint.x - range
        val clamped = xDisplacement.coerceIn(-range, range)
        return (clamped / range).toFloat()
    }

    private fun calculateAcceleration(touchPoint: Float2): Float {
        val range = screenSize.y / 2.0
        val yDisplacement = screenSize.y - touchPoint.y - screenSize.y / 4.0
        val clamped = yDisplacement.coerceIn(0.0, range)
        return (clamped / range).toFloat()
    }

    enum class TouchType constructor(val value: Int) {
        STEER(1),
        ACCELERATE(2)
    }

    interface ControlViewListener {
        fun onSteer(angle: Float)
        fun onAccelerate(power: Float)
        fun returnToCenter()
    }
}