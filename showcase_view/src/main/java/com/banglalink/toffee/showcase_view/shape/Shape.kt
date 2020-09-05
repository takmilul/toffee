package com.banglalink.toffee.showcase_view.shape

import android.graphics.Canvas
import android.graphics.Paint
import com.banglalink.toffee.showcase_view.target.Target

/**
 * Specifies a shape of the target (e.g circle, rectangle).
 * Implementations of this interface will be responsible to draw the shape
 * at specified center point (x, y).
 */
interface Shape {
    /**
     * Draw shape on the canvas with the center at (x, y) using Paint object provided.
     */
    fun draw(canvas: Canvas?, paint: Paint?, x: Int, y: Int)
    
    /**
     * Get width of the shape.
     */
    val width: Int
    
    /**
     * Get height of the shape.
     */
    val height: Int
    
    /**
     * Update shape bounds if necessary
     */
    fun updateTarget(target: Target?)
    val totalRadius: Int
    fun setPadding(padding: Int)
}