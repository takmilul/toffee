package com.banglalink.toffee.ui.player

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.PlayerOverlaySecondsBinding

/**
 * Layout group which handles the icon animation while forwarding and rewinding.
 *
 * Since it's based on view's alpha the fading effect is more fluid (more YouTube-like) than
 * using static drawables, especially when [cycleDuration] is low.
 *
 * Used by [YouTubeOverlay][com.github.vkay94.dtpv.youtube.YouTubeOverlay].
 */
class SecondsView(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {

    private val binding =
        PlayerOverlaySecondsBinding.inflate(LayoutInflater.from(context), this, true)


    /**
     * Defines the duration for a full cycle of the triangle animation.
     * Each animation step takes 20% of it.
     */
    var cycleDuration: Long = 750L
        set(value) {
            firstAnimator.duration = value / 5
            secondAnimator.duration = value / 5
            thirdAnimator.duration = value / 5
            fourthAnimator.duration = value / 5
            fifthAnimator.duration = value / 5
            field = value
        }

    /**
     * Sets the `TextView`'s seconds text according to the device`s language.
     */
    var seconds: Int = 0
        set(value) {
            binding.tvSeconds.text = "$value seconds"
            field = value
        }

    /**
     * Mirrors the triangles depending on what kind of type should be used (forward/rewind).
     */
    var isForward: Boolean = true
        set(value) {
            binding.triangleContainer.rotation = if (value) 0f else 180f
            field = value
        }

    val textView: TextView
        get() = binding.tvSeconds

    @DrawableRes
    var icon: Int = R.drawable.ic_player_play
        set(value) {
            if (value > 0) {
                binding.icon1.setImageResource(value)
                binding.icon2.setImageResource(value)
                binding.icon3.setImageResource(value)
            }
            field = value
        }

    /**
     * Starts the triangle animation
     */
    fun start() {
        stop()
        firstAnimator.start()
    }

    /**
     * Stops the triangle animation
     */
    fun stop() {
        firstAnimator.cancel()
        secondAnimator.cancel()
        thirdAnimator.cancel()
        fourthAnimator.cancel()
        fifthAnimator.cancel()
        reset()
    }

    private fun reset() {
        binding.icon1.alpha = 0f
        binding.icon2.alpha = 0f
        binding.icon3.alpha = 0f
    }

    private val firstAnimator: ValueAnimator = CustomValueAnimator(
        {
            binding.icon1.alpha = 0f
            binding.icon2.alpha = 0f
            binding.icon3.alpha = 0f
        }, {
            binding.icon1.alpha = it
        }, {
            secondAnimator.start()
        }
    )

    private val secondAnimator: ValueAnimator = CustomValueAnimator(
        {
            binding.icon1.alpha = 1f
            binding.icon2.alpha = 0f
            binding.icon3.alpha = 0f
        }, {
            binding.icon2.alpha = it
        }, {
            thirdAnimator.start()
        }
    )

    private val thirdAnimator: ValueAnimator = CustomValueAnimator(
        {
            binding.icon1.alpha = 1f
            binding.icon2.alpha = 1f
            binding.icon3.alpha = 0f
        }, {
            binding.icon1.alpha = 1f - binding.icon3.alpha // or 1f - it (t3.alpha => all three stay a little longer together)
            binding.icon3.alpha = it
        }, {
            fourthAnimator.start()
        }
    )

    private val fourthAnimator: ValueAnimator = CustomValueAnimator(
        {
            binding.icon1.alpha = 0f
            binding.icon2.alpha = 1f
            binding.icon3.alpha = 1f
        }, {
            binding.icon2.alpha = 1f - it
        }, {
            fifthAnimator.start()
        }
    )

    private val fifthAnimator: ValueAnimator = CustomValueAnimator(
        {
            binding.icon1.alpha = 0f
            binding.icon2.alpha = 0f
            binding.icon3.alpha = 1f
        }, {
            binding.icon3.alpha = 1f - it
        }, {
            firstAnimator.start()
        }
    )

    private inner class CustomValueAnimator(
        start: () -> Unit, update: (value: Float) -> Unit, end: () -> Unit
    ): ValueAnimator() {

        init {
            duration = cycleDuration / 5
            setFloatValues(0f, 1f)

            addUpdateListener { update(it.animatedValue as Float) }
            doOnStart { start() }
            doOnEnd { end() }
        }
    }
}