package com.banglalink.toffee.ui.bubble.util

import android.view.MotionEvent
import android.view.VelocityTracker

internal class VelocityTrackerHelper {
    
    // Velocity Tracker for Fling Animation
    var velocityX: Float = 0F
    var velocityY: Float = 0F
    private var velocityTracker: VelocityTracker? = null
    
    fun setVelocityTracker(event: MotionEvent) {
        velocityTracker?.clear()
        velocityTracker = velocityTracker ?: VelocityTracker.obtain()
        velocityTracker?.addMovement(event)
    }
    
    fun calculateVelocity(event: MotionEvent) {
        velocityTracker?.apply {
            val pointerId = event.getPointerId(event.actionIndex)
            addMovement(event)
            computeCurrentVelocity(5)
            velocityX = getXVelocity(pointerId)
            velocityY = getYVelocity(pointerId)
        }
    }
    
    fun recycleVelocityTracker() {
        velocityTracker?.recycle()
        velocityTracker = null
    }
}