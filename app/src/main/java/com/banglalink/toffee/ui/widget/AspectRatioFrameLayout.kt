package com.banglalink.toffee.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.IntDef
import kotlin.math.abs

/**
 * A [FrameLayout] that resizes itself to match a specified aspect ratio.
 */
class AspectRatioFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    /**
     * Resize modes for [AspectRatioFrameLayout].
     */
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(RESIZE_MODE_FIT, RESIZE_MODE_FIXED_WIDTH, RESIZE_MODE_FIXED_HEIGHT, RESIZE_MODE_FILL)
    annotation class ResizeMode

    private var videoAspectRatio = 0f
    private var resizeMode: Int

    /**
     * Set the aspect ratio that this view should satisfy.
     *
     * @param widthHeightRatio The width to height ratio.
     */
    fun setAspectRatio(widthHeightRatio: Float) {
        if (videoAspectRatio != widthHeightRatio) {
            videoAspectRatio = widthHeightRatio
            requestLayout()
        }
    }

    fun getAspectRatio(): Float = videoAspectRatio

    /**
     * Sets the resize mode.
     *
     * @param resizeMode The resize mode.
     */
    fun setResizeMode(@ResizeMode resizeMode: Int) {
        if (this.resizeMode != resizeMode) {
            this.resizeMode = resizeMode
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (resizeMode == RESIZE_MODE_FILL || videoAspectRatio <= 0) {
            // Aspect ratio not set.
            return
        }
        var width: Int = measuredWidth
        var height: Int = measuredHeight
        val viewAspectRatio = width.toFloat() / height
        val aspectDeformation = videoAspectRatio / viewAspectRatio - 1
        if (abs(aspectDeformation) <= MAX_ASPECT_RATIO_DEFORMATION_FRACTION) {
            // We're within the allowed tolerance.
            return
        }
        when (resizeMode) {
            RESIZE_MODE_FIXED_WIDTH -> height = (width / videoAspectRatio).toInt()
            RESIZE_MODE_FIXED_HEIGHT -> width = (height * videoAspectRatio).toInt()
            else -> if (aspectDeformation > 0) {
                height = (width / videoAspectRatio).toInt()
            } else {
                width = (height * videoAspectRatio).toInt()
            }
        }
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    companion object {
        /**
         * Either the width or height is decreased to obtain the desired aspect ratio.
         */
        const val RESIZE_MODE_FIT = 0

        /**
         * The width is fixed and the height is increased or decreased to obtain the desired aspect ratio.
         */
        const val RESIZE_MODE_FIXED_WIDTH = 1

        /**
         * The height is fixed and the width is increased or decreased to obtain the desired aspect ratio.
         */
        const val RESIZE_MODE_FIXED_HEIGHT = 2

        /**
         * The specified aspect ratio is ignored.
         */
        const val RESIZE_MODE_FILL = 3

        /**
         * The [FrameLayout] will not resize itself if the fractional difference between its natural
         * aspect ratio and the requested aspect ratio falls below this threshold.
         *
         *
         * This tolerance allows the view to occupy the whole of the screen when the requested aspect
         * ratio is very close, but not exactly equal to, the aspect ratio of the screen. This may reduce
         * the number of view layers that need to be composited by the underlying system, which can help
         * to reduce power consumption.
         */
        private const val MAX_ASPECT_RATIO_DEFORMATION_FRACTION = 0.01f
    }

    init {
        resizeMode = RESIZE_MODE_FIT
    }
}