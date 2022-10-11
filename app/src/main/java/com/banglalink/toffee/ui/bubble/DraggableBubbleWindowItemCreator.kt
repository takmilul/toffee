package com.banglalink.toffee.ui.bubble

import android.annotation.SuppressLint
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemGravity
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemTouchEvent.*
import com.banglalink.toffee.ui.bubble.listener.IBubbleDraggableWindowItemEventListener
import com.banglalink.toffee.ui.bubble.util.*
import kotlin.math.abs

/**
 * This makes the view draggable on a window
 */
class DraggableWindowItemCreator {
    
    private var listener: IBubbleDraggableWindowItemEventListener? = null
    private var velocityTrackerHelper: VelocityTrackerHelper = VelocityTrackerHelper()
    
    fun setViewAsDraggableItemOnWindow(
        view: View,
        windowManager: WindowManager,
        params: WindowManager.LayoutParams,
        listener: IBubbleDraggableWindowItemEventListener? = null,
        gravity: DraggableWindowItemGravity
    ): DraggableWindowItemCreator {
        view.setOnTouchListener(DraggableViewTouchListener(windowManager, params, gravity))
        this.listener = listener
        
        return this
    }
    
    private inner class DraggableViewTouchListener(
        private val windowManager: WindowManager,
        private val params: WindowManager.LayoutParams,
        private val gravity: DraggableWindowItemGravity
    ) : View.OnTouchListener {
        // Track the initial position of the view in the window
        private var prevX: Int = params.x
        private var prevY: Int = params.y
        
        // Track the initial touch position
        private var prevTouchX: Float = -1F
        private var prevTouchY: Float = -1F
        
        private var deltaX: Float = -1F
        private var deltaY: Float = -1F
        
        private var lastAction: Int? = null
        
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val currentTouchPoint = Point(event.rawX.toInt(), event.rawY.toInt())
            
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    velocityTrackerHelper.setVelocityTracker(event)
                    
                    // To make sure that the touch spot is correctly representing the position of the draggable view
//                    alignTouchToViewCenter(view, currentTouchPoint)
                    
                    prevX = params.x
                    prevY = params.y
                    
                    prevTouchX = event.rawX
                    prevTouchY = event.rawY
                    
//                    Log.i("bubble_", "prevX: $prevX \nprevY: $prevY \nprevTouchX: $prevTouchX \nprevTouchY: $prevTouchY")
                    
                    lastAction = event.action
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (deltaX > 10 || deltaX < -10 || deltaY > 10 || deltaY < -10) {
                        listener?.onTouchEventChanged(
                            view,
                            Point(params.x, params.y),
                            currentTouchPoint,
                            velocityTrackerHelper.velocityX,
                            velocityTrackerHelper.velocityY,
                            DRAG_STOP_EVENT
                        )
                    } else {
                        listener?.onTouchEventChanged(
                            view,
                            Point(params.x, params.y),
                            currentTouchPoint,
                            velocityTrackerHelper.velocityX,
                            velocityTrackerHelper.velocityY,
                            CLICK_EVENT
                        )
                    }
                    
                    lastAction = event.action
                    velocityTrackerHelper.recycleVelocityTracker()
                    
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    velocityTrackerHelper.calculateVelocity(event)
                    
                    // Calculate motion difference between previous touch point and current touch point
                    deltaX = event.rawX - prevTouchX
                    deltaY = event.rawY - prevTouchY
                    
                    // Calculate x based on initial gravity position or else it'll go the opposite direction.
                    // prevX holds the x position based on where it gravitates towards. If the view gravitates
                    // towards the left, we should add to the previous x position else subtract from previous x
                    // position because the view gravitates towards the right side
                    params.x = if (isViewGravityLeft(gravity)) prevX + deltaX.toInt() else prevX - deltaX.toInt()
                    
                    // Calculate y based on initial gravity position or else it'll go the opposite direction.
                    // prevY holds the y position based on where it gravitates towards. If the view gravitates
                    // towards the top, we should add to the previous y position else subtract from previous y
                    // position because the view gravitates towards the bottom
                    params.y = if (isViewGravityTop(gravity)) prevY + deltaY.toInt() else prevY - deltaY.toInt()
                    
//                    Log.i("bubble_", "isViewGravityLeft: ${isViewGravityLeft(gravity)} \nisViewGravityTop: ${isViewGravityTop(gravity)}")
//                    Log.i("bubble_", "event.rawX: ${event.rawX} \nevent.rawY: ${event.rawY} \ndeltaX: $deltaX \ndeltaY: $deltaY \nparams.x: ${params.x} \nparams.y: ${params.y}")
                    
                    if (deltaX > 10 || deltaX < -10 || deltaY > 10 || deltaY < -10) {
                        
                        windowManager.updateViewLayout(view, params)
                        
                        listener?.onTouchEventChanged(
                            view,
                            Point(params.x, params.y),
                            currentTouchPoint,
                            velocityTrackerHelper.velocityX,
                            velocityTrackerHelper.velocityY,
                            DRAG_EVENT
                        )
                    }
                    lastAction = event.action
                    return true
                }
                else -> return false
            }
        }
        
        /**
         * Align the touch position to the view center based on gravity
         */
        fun alignTouchToViewCenter(view: View, currentTouchPoint: Point) {
            if (isViewGravityLeft(gravity)) {
                params.x = currentTouchPoint.x - view.width / 2
            } else {
                params.x = abs(currentTouchPoint.x - getScreenWidth(windowManager) + view.width / 2)
            }
            
            if (isViewGravityTop(gravity)) {
                params.y = currentTouchPoint.y - view.height
            } else {
                params.y = getScreenHeight(windowManager) - (currentTouchPoint.y + view.height / 2)
            }
            windowManager.updateViewLayout(view, params)
        }
    }
}