package com.banglalink.toffee.ui.bubble.view.builder

import android.view.View
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemGravity
import com.banglalink.toffee.ui.bubble.listener.IBubbleDraggableWindowItemEventListener
import com.banglalink.toffee.ui.bubble.view.BubbleDraggableItem

interface IBubbleDraggableItemBuilder {
    fun setLayout(layout: View): IBubbleDraggableItemBuilder
    fun setGravity(gravity: DraggableWindowItemGravity): IBubbleDraggableItemBuilder // The starting placement value for the draggable item
    fun setStartingXPosition(pos: Int): IBubbleDraggableItemBuilder // The horizontal starting position value of the draggable item on the x-axis
    fun setStartingYPosition(pos: Int): IBubbleDraggableItemBuilder // The vertical starting position value of the draggable item on the y-axis
    fun setListener(listener: IBubbleDraggableWindowItemEventListener): IBubbleDraggableItemBuilder
    fun build(): BubbleDraggableItem
}