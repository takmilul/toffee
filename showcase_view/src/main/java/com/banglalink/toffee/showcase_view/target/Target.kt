package com.banglalink.toffee.showcase_view.target

import android.graphics.Point
import android.graphics.Rect

interface Target {
    val point: Point
    val bounds: Rect
    
    companion object {
        val NONE: Target = object : Target {
            override val point: Point
                get() = Point(1000000, 1000000)
            override val bounds: Rect
                get() {
                    val p = point
                    return Rect(p.x - 190, p.y - 190, p.x + 190, p.y + 190)
                }
        }
    }
}