package com.banglalink.toffee.ui.bubble.listener

import android.graphics.Point
import android.view.View
import com.banglalink.toffee.ui.bubble.enums.DraggableWindowItemTouchEvent

interface IBubbleDraggableWindowItemEventListener {
    fun onTouchEventChanged(
        view: View,
        currentViewPosition: Point,
        currentTouchPoint: Point,
        velocityX: Float,
        velocityY: Float,
        draggableWindowItemTouchEvent: DraggableWindowItemTouchEvent
    )
}