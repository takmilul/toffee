package com.banglalink.toffee.showcase_view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Point
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.banglalink.toffee.showcase_view.IAnimationFactory.AnimationEndListener
import com.banglalink.toffee.showcase_view.IAnimationFactory.AnimationStartListener

class FadeAnimationFactory : IAnimationFactory {
    private val interpolator: AccelerateDecelerateInterpolator
    override fun animateInView(target: View?, point: Point?, duration: Long, listener: AnimationStartListener?) {
        val oa = ObjectAnimator.ofFloat(target, ALPHA, INVISIBLE, VISIBLE)
        oa.setDuration(duration).addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                listener!!.onAnimationStart()
            }
    
            override fun onAnimationEnd(animator: Animator) {}
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        oa.start()
    }
    
    override fun animateOutView(target: View?, point: Point?, duration: Long, listener: AnimationEndListener?) {
        val oa = ObjectAnimator.ofFloat(target, ALPHA, INVISIBLE)
        oa.setDuration(duration).addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                listener!!.onAnimationEnd()
            }
    
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
        oa.start()
    }
    
    override fun animateTargetToPoint(showcaseView: MaterialShowcaseView?, point: Point?) {
        val set = AnimatorSet()
        val xAnimator = ObjectAnimator.ofInt(showcaseView, "showcaseX", point!!.x)
        val yAnimator = ObjectAnimator.ofInt(showcaseView, "showcaseY", point.y)
        set.playTogether(xAnimator, yAnimator)
        set.interpolator = interpolator
        set.start()
    }
    
    companion object {
        private const val ALPHA = "alpha"
        private const val INVISIBLE = 0f
        private const val VISIBLE = 1f
    }
    
    init {
        interpolator = AccelerateDecelerateInterpolator()
    }
}