package com.banglalink.toffee.ui.bubble.view.builder

import android.content.Context
import com.banglalink.toffee.ui.bubble.view.BubbleCloseItem

interface IBubbleRemoveItemBuilder {
    fun with(context: Context): IBubbleRemoveItemBuilder
    fun setRemoveXDrawable(drawable: Int): IBubbleRemoveItemBuilder
    fun setShouldFollowDrag(shouldFollowDrag: Boolean): IBubbleRemoveItemBuilder /* Flag to determine if the remove view should animate and follow the dragged item */
    fun setExpandable(shouldExpand: Boolean): IBubbleRemoveItemBuilder /* Flag to determine if the remove view should expand to the size of the draggable item */
    fun build(): BubbleCloseItem
}