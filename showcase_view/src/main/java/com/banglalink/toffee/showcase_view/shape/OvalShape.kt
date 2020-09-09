package com.banglalink.toffee.showcase_view.shape

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.banglalink.toffee.showcase_view.target.Target

class OvalShape : Shape {
    override var height: Int
        get() = field
        set
    var isAdjustToTarget: Boolean
    private var padding = 0
    
    constructor() {
        height = 200
        isAdjustToTarget = true
    }
    
    constructor(radius: Int) {
        height = 200
        isAdjustToTarget = true
        height = radius
    }
    
    constructor(bounds: Rect) : this(getPreferredRadius(bounds)) {}
    constructor(target: Target) : this(target.bounds) {}
    
    override fun draw(canvas: Canvas?, paint: Paint?, x: Int, y: Int) {
        if (height > 0) {
            val rad = (height + padding).toFloat()
            val rectF = RectF(x - rad, y - rad / 2, x + rad, y + rad / 2)
            canvas!!.drawOval(rectF, paint!!)
        }
    }
    
    override fun updateTarget(target: Target?) {
        if (isAdjustToTarget) {
            height = getPreferredRadius(target!!.bounds)
        }
    }
    
    override val totalRadius: Int
        get() = height + padding
    
    override fun setPadding(padding: Int) {
        this.padding = padding
    }
    
    override val width: Int
        get() = height * 2
    
    companion object {
        fun getPreferredRadius(bounds: Rect): Int {
            return Math.max(bounds.width(), bounds.height()) / 2
        }
    }
}