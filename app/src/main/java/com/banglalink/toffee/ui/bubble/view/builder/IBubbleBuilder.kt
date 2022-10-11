package com.banglalink.toffee.ui.bubble.view.builder

import android.content.Context
import com.banglalink.toffee.ui.bubble.Bubble
import com.banglalink.toffee.ui.bubble.listener.IBubbleInteractionListener
import com.banglalink.toffee.ui.bubble.view.BubbleCloseItem
import com.banglalink.toffee.ui.bubble.view.BubbleDraggableItem

interface IBubbleBuilder {
    fun with(context: Context): IBubbleBuilder
    fun setDraggableItem(view: BubbleDraggableItem): IBubbleBuilder
    fun setRemoveItem(view: BubbleCloseItem): IBubbleBuilder
    fun setListener(listener: IBubbleInteractionListener): IBubbleBuilder
    fun build(): Bubble
}