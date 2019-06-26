package com.example.pc.oreo

import android.content.Context
import android.graphics.Point
import android.media.MediaCodec
import android.media.MediaFormat
import android.util.AttributeSet
import android.view.SurfaceView
import io.reactivex.disposables.CompositeDisposable
import java.nio.ByteBuffer
import kotlin.experimental.and

class DecoderView : SurfaceView {
    private var codec: MediaCodec? = null
    private var configured: Boolean = false
    //pic mode sequenceParameterSet
    private var sequenceParameterSet = byteArrayOf(0, 0, 0, 1, 103, 77, 64, 40, -107, -96, 60, 5, -71)
    //vid mode sequenceParameterSet
    private val vidSps = intArrayOf(0, 0, 0, 1, 103, 77, 64, 40, 149, 160, 20, 1, 110, 64)
    private val pictureParameterSet = byteArrayOf(0, 0, 0, 1, 104, -18, 56, -128)
    private var decoderWidth = 960
    private val decoderHeight = 720
    private val compositeDisposable = CompositeDisposable()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    fun init() {
        decoderWidth = if (sequenceParameterSet.size == 14) 1280 else 960

        val videoFormat = MediaFormat.createVideoFormat("video/avc", decoderWidth, decoderHeight)
        videoFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sequenceParameterSet))
        videoFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pictureParameterSet))

        val str = videoFormat.getString("mime")
        try {
            val cdx = MediaCodec.createDecoderByType(str)
            cdx.configure(videoFormat, holder.surface, null, 0)
            cdx.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            cdx.start()

            codec = cdx
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

        val nalType = frame[4] and 0x1f
        if (nalType == 7.toByte()) {
            if (frame.size != sequenceParameterSet.size) {
                release()
                sequenceParameterSet = frame
                init()
            }
            return
        }
        if (nalType == 8.toByte() || nalType == 5.toByte() || !configured) {
            return
        }

        try {
            codec?.let {
                val dequeueInputBuffer = it.dequeueInputBuffer(-1L)
                dequeueInputBuffer.let {
                    if (it >= 0) {
                        val byteBuffer = codec?.getInputBuffer(it)
                        if (byteBuffer != null) {
                            byteBuffer.put(frame)
                            codec?.queueInputBuffer(dequeueInputBuffer, 0, frame.size, 0L, 0)
                        }
                    }
                }

                val bufferInfo = MediaCodec.BufferInfo()
                var i = it.dequeueOutputBuffer(bufferInfo, 0L)
                while (i >= 0) {
                    it.releaseOutputBuffer(i, true)
                    it.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT)

                    i = it.dequeueOutputBuffer(bufferInfo, 0L)
                }
            }
        } catch (ex: Exception) {
            release()
        }

    }

    fun release() {
        configured = false
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        codec?.let {
            codec?.release()
            codec = null
        }
    }
}
