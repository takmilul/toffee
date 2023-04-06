package com.banglalink.toffee.ui.bubble

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.view.Gravity
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.WindowManager
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemGravity
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemTouchEvent
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemTouchEvent.*
import com.banglalink.toffee.ui.bubble.listener.IBubbleDraggableWindowItemEventListener
import com.banglalink.toffee.ui.bubble.listener.IBubbleInteractionListener
import com.banglalink.toffee.ui.bubble.util.*
import com.banglalink.toffee.ui.bubble.view.BubbleCloseItem
import com.banglalink.toffee.ui.bubble.view.BubbleDraggableItem
import com.banglalink.toffee.ui.bubble.view.builder.IBubbleBuilder
import java.util.concurrent.atomic.*
import kotlin.math.abs

/**
 * Manages all the views in the window
 */
class Bubble(builder: Builder) {
    
    private val context = builder.context
    private val draggableItem = builder.draggableItem
    private val removeItem = builder.removeItem
    
    private val interactionListener = builder.listener
    
    private var draggableItemCreator: DraggableWindowItemCreator? = null
    
    private val isAnimatingShowRemoveItem = AtomicBoolean(false)
    private val isAnimatingHideRemoveItem = AtomicBoolean(false)
    private val isAnimatingResizeRemoveItem = AtomicBoolean(false)
    
    private val isFlingAnimationLocked = AtomicBoolean(false)
    private val isVibrateLocked = AtomicBoolean(false)
    
    private val windowManager = context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
    
    init {
        addViewToWindow()
    }
    
    private fun addViewToWindow() {
        try {
            windowManager.addView(removeItem.removeLayout, getDefaultRemoveItemWindowParams())
            windowManager.addView(draggableItem.view, getDefaultDraggableItemWindowParams())
            draggableItem.view.makeDraggable()
        }catch (_:Exception){
        
        }

    }
    
    fun removeViewFromWindow() {
        try {
            windowManager.removeView(draggableItem.view)
            windowManager.removeView(removeItem.removeLayout)
        }catch (_:Exception){
        
        }
    }
    
    private fun getDefaultDraggableItemWindowParams(): WindowManager.LayoutParams {
        val windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (isOreoOrAbove()) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        // Specific initial draggable view position and placement in the window
        windowParams.gravity = draggableItem.gravity.value
        windowParams.x = draggableItem.startingXPosition
        windowParams.y = draggableItem.startingYPosition
        
        return windowParams
    }
    
    private fun getDefaultRemoveItemWindowParams(): WindowManager.LayoutParams {
        val windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (isOreoOrAbove()) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        windowParams.gravity = Gravity.BOTTOM or Gravity.CENTER
        
        return windowParams
    }
    
    private fun View.makeDraggable() {
        draggableItemCreator = DraggableWindowItemCreator().setViewAsDraggableItemOnWindow(
            this, windowManager, getDefaultDraggableItemWindowParams(), DraggableWindowItemEventListener(), draggableItem.gravity
        )
    }
    
    /**
     * Listener to react to the touch events from the draggable item
     */
    inner class DraggableWindowItemEventListener : IBubbleDraggableWindowItemEventListener {
        
        override fun onTouchEventChanged(
            view: View,
            currentViewPosition: Point,
            currentTouchPoint: Point,
            velocityX: Float,
            velocityY: Float,
            draggableWindowItemTouchEvent: DraggableWindowItemTouchEvent
        ) {
            draggableItem.listener?.onTouchEventChanged(view, currentViewPosition, currentTouchPoint, velocityX, velocityY, draggableWindowItemTouchEvent)
            
            when (draggableWindowItemTouchEvent) {
                CLICK_EVENT -> {
                    /* Do Nothing */
                }
                DRAG_EVENT -> {
                    animateRemoveItemVisibility(shouldShow = true, isInDeleteMode = false)
                    
                    if (removeItem.removeLayout.isInBounds(currentTouchPoint.x, currentTouchPoint.y)) {
                        interactionListener?.onOverlappingRemoveItemOnDrag(removeItem, draggableItem)
                        
                        // Expand the remove view and center the view to the remove view
                        animateRemoveItemExpand(true)
                        
                        // Vibrate once to notify that the draggable item is within the remove item
                        if (isVibrateLocked.compareAndSet(false, true)) {
                            removeItem.removeLayout.vibrate()
                        }
                    } else {
                        animateRemoveItemExpand(false)
                        
                        isVibrateLocked.set(false) // Reset to allow vibrate again when draggable item re-enter remove item
                        interactionListener?.onNotOverlappingRemoveItemOnDrag(removeItem, draggableItem)
                    }
                }
                DRAG_STOP_EVENT -> {
                    if (removeItem.removeLayout.isInBounds(currentTouchPoint.x, currentTouchPoint.y)) {
                        // Lock fling animation on draggable item and hide draggable item
                        isFlingAnimationLocked.set(true)
                        
                        animateRemoveItemVisibility(shouldShow = false, isInDeleteMode = true)
                        
                        view.createMoveYAndResizeAnimator(
                            destY = BELOW_SCREEN_Y_POSITION, scaleX = INVISIBLE_SCALE, scaleY = INVISIBLE_SCALE, duration = DURATION_IN_MILLIS
                        ).start()
                        
                        interactionListener?.onDropInRemoveItem(removeItem, draggableItem)
                    } else {
                        val destX = calculateFlingDestinationX(view, currentViewPosition.x, velocityX, draggableItem.gravity)
                        val destY = calculateFlingDestinationY(currentViewPosition.y, velocityY, draggableItem.gravity)
                        
                        if (isFlingAnimationLocked.compareAndSet(false, true)) {
                            // Fling the draggable item to calculated destination
                            createFlingAnimation(
                                view = view, initialPos = currentViewPosition, destX = destX, destY = destY
                            ).start()
                        }
                        animateRemoveItemVisibility(shouldShow = false, isInDeleteMode = false)
                    }
                }
            }
        }
        
        private fun createFlingAnimation(view: View, initialPos: Point, destX: Float, destY: Float): ViewPropertyAnimator {
            return view.createFlingAnimator(FLING_ANIMATION_DURATION, FLING_ANIMATION_TENSION, {
                val percent = it.animatedFraction
                
                val deltaX = destX - initialPos.x
                val deltaY = destY - initialPos.y
                
                val params = getDefaultDraggableItemWindowParams()
                
                params.x = if (isViewGravityLeft(draggableItem.gravity)) initialPos.x + (deltaX * percent).toInt() else initialPos.x - (deltaX * percent).toInt()
                params.y = if (isViewGravityTop(draggableItem.gravity)) initialPos.y + (deltaY * percent).toInt() else initialPos.y - (deltaY * percent).toInt()
                
                windowManager.updateViewLayout(view, params)
                
            }, object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isFlingAnimationLocked.set(false)
                    super.onAnimationEnd(animation)
                }
            })
        }
        
        private fun calculateFlingDestinationX(view: View, currentViewPosX: Int, velocityX: Float, gravity: DraggableWindowItemGravity): Float {
            val screenWidth = getScreenWidth(windowManager)
            val startingEdgePos = 0
            
            val widthOfViewHalf = view.width / 2
            
            val isLeftToRightScreen = isViewGravityLeft(gravity)
            
            // Calculate the final destination of the view based on the gravity (wherever it starts is the 0 pos)
            var destinationX = if (isLeftToRightScreen) {
                if (currentViewPosX + widthOfViewHalf > screenWidth / 2) screenWidth else startingEdgePos
            } else {
                if (currentViewPosX - widthOfViewHalf < screenWidth / 2) startingEdgePos else screenWidth
            }
            
            // If the fling is strong then we should move to the opposite side
            if (abs(velocityX) > 25) {
                destinationX = if (isLeftToRightScreen) {
                    if (velocityX > startingEdgePos) screenWidth else startingEdgePos // Check if going opposite direction from start pos using the velocity
                } else {
                    if (velocityX > startingEdgePos) startingEdgePos else screenWidth
                }
            }
            return destinationX.toFloat()
        }
        
        private fun calculateFlingDestinationY(currentViewPosY: Int, velocityY: Float, gravity: DraggableWindowItemGravity): Float {
            val screenHeight = getScreenHeight(windowManager)
            val startingEdgePos = 0F // represent the top or bottom of screen depending on the starting gravity
            
            val isTopToBottomScreen = isViewGravityTop(gravity)
            
            var destinationY = if (isTopToBottomScreen) {
                currentViewPosY + (velocityY * 10)
            } else {
                currentViewPosY - (velocityY * 10)
            }
            
            // If the destination calculated is pass the top or bottom of the screen, adjust to either the top or bottom respectively
            if (destinationY <= startingEdgePos) {
                destinationY = startingEdgePos
            } else if (destinationY >= screenHeight) {
                destinationY = screenHeight.toFloat()
            }
            
            return destinationY
        }
    }
    
    internal fun animateRemoveItemVisibility(shouldShow: Boolean, isInDeleteMode: Boolean) {
        val removeView = removeItem.removeLayout
        
        if (shouldShow) {
            if (isAnimatingShowRemoveItem.compareAndSet(false, true)) {
                removeItem.setVisible(shouldShow)
                
                removeView.createMoveYAndResizeAnimator(
                    STARTING_POSITION,
                    NORMAL_SCALE,
                    NORMAL_SCALE,
                    DURATION_IN_MILLIS,
                    null,
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            isAnimatingShowRemoveItem.set(false)
                        }
                    }).start()
            }
        } else {
            if (isAnimatingHideRemoveItem.compareAndSet(false, true)) {
                removeView.createMoveYAndResizeAnimator(
                    BELOW_SCREEN_Y_POSITION,
                    INVISIBLE_SCALE,
                    INVISIBLE_SCALE,
                    DURATION_IN_MILLIS,
                    null,
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            isAnimatingHideRemoveItem.set(false)
                            removeItem.setVisible(shouldShow)
                            if (isInDeleteMode) {
                                BaseBubbleService.isForceClosed = true
                                BaseBubbleService.INSTANCE.stopSelf() // Stop the Service
                            }
                        }
                    }
                ).start()
            }
        }
    }
    
    private fun animateRemoveItemExpand(shouldExpand: Boolean) {
        if (isAnimatingResizeRemoveItem.compareAndSet(false, true)) {
            if (shouldExpand && removeItem.expandable) {
                removeItem.removeCircleView.createResizeAnimator(1.3F, 1.3F, 50L, object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        isAnimatingResizeRemoveItem.set(false)
                        super.onAnimationEnd(animation)
                    }
                }).start()
            } else {
                isAnimatingResizeRemoveItem.set(true)
                removeItem.removeCircleView.createResizeAnimator(1F, 1F, 50L, object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        isAnimatingResizeRemoveItem.set(false)
                        super.onAnimationEnd(animation)
                    }
                }).start()
            }
        }
    }
    
    class Builder : IBubbleBuilder {
        
        lateinit var context: Context
        lateinit var removeItem: BubbleCloseItem
        lateinit var draggableItem: BubbleDraggableItem
        var listener: IBubbleInteractionListener? = null
        
        override fun with(context: Context): IBubbleBuilder = this.apply { this.context = context }
        
        override fun setDraggableItem(view: BubbleDraggableItem) = this.apply { draggableItem = view }
        
        override fun setRemoveItem(view: BubbleCloseItem) = this.apply { removeItem = view }
        
        override fun setListener(listener: IBubbleInteractionListener) = this.apply { this.listener = listener }
        
        override fun build(): Bubble {
            if (!this::context.isInitialized) throw IllegalStateException(BUBBLE_BUIlDER_NO_CONTEXT)
            if (!this::draggableItem.isInitialized) throw IllegalStateException(BUBBLE_BUIlDER_NO_DRAGGABLE_ITEM)
            if (!this::removeItem.isInitialized) throw IllegalStateException(BUBBLE_BUIlDER_NO_REMOVE_ITEM)
            return Bubble(this)
        }
    }
}