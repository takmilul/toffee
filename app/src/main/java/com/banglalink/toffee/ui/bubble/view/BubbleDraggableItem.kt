package com.banglalink.toffee.ui.bubble.view

import android.view.View
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemGravity
import com.banglalink.toffee.ui.bubble.listener.IBubbleDraggableWindowItemEventListener
import com.banglalink.toffee.ui.bubble.util.DRAGGABLE_ITEM_BUIDLER_NO_LAYOUT
import com.banglalink.toffee.ui.bubble.view.builder.IBubbleDraggableItemBuilder

/**
 * BubbleDraggableItem: The item that can be dragged around the screen
 */
class BubbleDraggableItem(builder: Builder) {
    
    val view = builder.layout
    val gravity = builder.gravity
    val startingXPosition = builder.startingXPosition
    val startingYPosition = builder.startingYPosition
    val listener = builder.listener
    
    /**
     * Builder that contains the construction code needed to build the draggable view
     */
    class Builder : IBubbleDraggableItemBuilder {
        lateinit var layout: View
        
        var gravity: DraggableWindowItemGravity = DraggableWindowItemGravity.TOP_RIGHT
        var startingXPosition: Int = 0
        var startingYPosition: Int = 100
        
        var listener: IBubbleDraggableWindowItemEventListener? = null
        
        override fun setLayout(layout: View) = this.apply { this.layout = layout }
        
        override fun setGravity(gravity: DraggableWindowItemGravity) = this.apply { this.gravity = gravity }
        
        override fun setStartingXPosition(pos: Int) = this.apply { startingXPosition = pos }
        
        override fun setStartingYPosition(pos: Int) = this.apply { startingYPosition = pos }
        
        override fun setListener(listener: IBubbleDraggableWindowItemEventListener) = this.apply { this.listener = listener }
        
        override fun build(): BubbleDraggableItem {
            if (!this::layout.isInitialized) throw IllegalStateException(
                DRAGGABLE_ITEM_BUIDLER_NO_LAYOUT
            )
            
            return BubbleDraggableItem(this)
        }
    }
}