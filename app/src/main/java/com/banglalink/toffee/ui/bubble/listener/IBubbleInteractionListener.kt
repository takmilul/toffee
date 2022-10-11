package com.banglalink.toffee.ui.bubble.listener

import com.banglalink.toffee.ui.bubble.view.BubbleCloseItem
import com.banglalink.toffee.ui.bubble.view.BubbleDraggableItem

interface IBubbleInteractionListener {
    fun onOverlappingRemoveItemOnDrag(removeItem: BubbleCloseItem, draggableItem: BubbleDraggableItem)
    fun onNotOverlappingRemoveItemOnDrag(removeItem: BubbleCloseItem, draggableItem: BubbleDraggableItem)
    fun onDropInRemoveItem(removeItem: BubbleCloseItem, draggableItem: BubbleDraggableItem)
}