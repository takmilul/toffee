package com.banglalink.toffee.showcase_view.shape

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.banglalink.toffee.showcase_view.target.Target

/**
 * Circular shape for target.
 */
class CircleShape : Shape {
    var radius = 200
    var isAdjustToTarget = true
    private var padding = 0
    
    constructor() {}
    constructor(radius: Int) {
        this.radius = radius
    }
    
    constructor(bounds: Rect) : this(getPreferredRadius(bounds)) {}
    constructor(target: Target) : this(target.bounds) {}
    
    override fun draw(canvas: Canvas?, paint: Paint?, x: Int, y: Int) {
        if (radius > 0) {
            canvas!!.drawCircle(x.toFloat(), y.toFloat(), radius + padding.toFloat(), paint!!)
        }
    }
    
    override fun updateTarget(target: Target?) {
        if (isAdjustToTarget) radius = getPreferredRadius(target!!.bounds)
    }
    
    override val totalRadius: Int
        get() = radius + padding
    
    override fun setPadding(padding: Int) {
        this.padding = padding
    }
    
    override val width: Int
        get() = radius * 2
    override val height: Int
        get() = radius * 2
    
    companion object {
        fun getPreferredRadius(bounds: Rect): Int {
            return Math.max(bounds.width(), bounds.height()) / 2
        }
    }
}