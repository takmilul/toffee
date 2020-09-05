package com.banglalink.toffee.showcase_view.shape

import android.graphics.Canvas
import android.graphics.Paint
import com.banglalink.toffee.showcase_view.target.Target

/**
 * A Shape implementation that draws nothing.
 */
class NoShape : Shape {
    override fun updateTarget(target: Target?) {
        // do nothing
    }
    
    override val totalRadius: Int
        get() = 0
    
    override fun setPadding(padding: Int) {
        // do nothing
    }
    
    override fun draw(canvas: Canvas?, paint: Paint?, x: Int, y: Int) {
        // do nothing
    }
    
    override val width: Int
        get() = 0
    override val height: Int
        get() = 0
}