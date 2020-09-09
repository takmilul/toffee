package com.banglalink.toffee.showcase_view.shape

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.banglalink.toffee.showcase_view.target.Target

class RectangleShape : Shape {
    private var fullWidth = false
    override var width = 0
        private set
    override var height = 0
        private set
    var isAdjustToTarget = true
    private var rect: Rect? = null
    private var padding = 0
    
    constructor(width: Int, height: Int) {
        this.width = width
        this.height = height
        init()
    }
    
    @JvmOverloads
    constructor(bounds: Rect, fullWidth: Boolean = false) {
        this.fullWidth = fullWidth
        height = bounds.height()
        width = if (fullWidth) Int.MAX_VALUE else bounds.width()
        init()
    }
    
    private fun init() {
        rect = Rect(-width / 2, -height / 2, width / 2, height / 2)
    }
    
    override fun draw(canvas: Canvas?, paint: Paint?, x: Int, y: Int) {
        if (!rect!!.isEmpty) {
            canvas!!.drawRect(
                rect!!.left + x - padding.toFloat(),
                rect!!.top + y - padding.toFloat(),
                rect!!.right + x + padding.toFloat(),
                rect!!.bottom + y + padding.toFloat(),
                paint!!
            )
        }
    }
    
    override fun updateTarget(target: Target?) {
        if (isAdjustToTarget) {
            val bounds = target!!.bounds
            height = bounds.height()
            width = if (fullWidth) Int.MAX_VALUE else bounds.width()
            init()
        }
    }
    
    override val totalRadius: Int
        get() = height / 2 + padding
    
    override fun setPadding(padding: Int) {
        this.padding = padding
    }
}