package com.example.pc.oreo

import android.content.Context
import android.graphics.Point
import android.media.MediaCodec
import android.media.MediaFormat
import android.util.AttributeSet
import android.view.SurfaceView
import java.nio.ByteBuffer
import kotlin.experimental.and

class DecoderView : SurfaceView {
    private var codec: MediaCodec? = null
    private var configured: Boolean = false
    private var sequenceParameterSet = byteArrayOf(0, 0, 0, 1, 103, 77, 64, 40, -107, -96, 60, 5, -71)
    private val pictureParameterSet = byteArrayOf(0, 0, 0, 1, 104, -18, 56, -128)
    private var decoderWidth = 1280
    private val decoderHeight = 720

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun init() {
        val videoFormat = MediaFormat.createVideoFormat("video/avc", decoderWidth, decoderHeight)
        var str: String

        videoFormat.apply {
            setByteBuffer("csd-0", ByteBuffer.wrap(sequenceParameterSet))
            setByteBuffer("csd-1", ByteBuffer.wrap(pictureParameterSet))
            str = getString("mime")
        }
        try {
            MediaCodec.createDecoderByType(str).apply {
                configure(videoFormat, holder.surface, null, 0)
                setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT)
                start()
                codec = this
            }
            configured = true
        } catch (ex: Exception) {
            configured = false
            return
        }

        (context as MainActivity).runOnUiThread {
            val videoProportion = decoderWidth.toFloat() / decoderHeight.toFloat()

            val size = Point()
            (context as MainActivity).windowManager.defaultDisplay.getSize(size)
            val screenWidth = size.x
            val screenHeight = size.y
            val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()

            val lp = layoutParams
            if (videoProportion > screenProportion) {
                lp.width = screenWidth
                lp.height = (screenWidth.toFloat() / videoProportion).toInt()
            } else {
                lp.width = (videoProportion * screenHeight.toFloat()).toInt()
                lp.height = screenHeight
            }
            layoutParams = lp
        }
    }

    fun decode(frame: ByteArray) {
        if (!configured) {
            init()
        }

        try {
            codec?.apply {
                dequeueInputBuffer(-1L).let {
                    if (it >= 0) {
                        getInputBuffer(it)?.put(frame)
                        queueInputBuffer(it, 0, frame.size, 0L, 0)
                    }
                }

                val bufferInfo = MediaCodec.BufferInfo()
                var i = dequeueOutputBuffer(bufferInfo, 0L)
                while (i >= 0) {
                    releaseOutputBuffer(i, true)
                    setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT)

                    i = dequeueOutputBuffer(bufferInfo, 0L)
                }
            }
        } catch (ex: Exception) {
            release()
        }

    }

    fun release() {
        configured = false
        codec?.let {
            it.release()
            codec = null
        }
    }
}
