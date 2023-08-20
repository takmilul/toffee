/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Douglas Nassif Roma Junior
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.banglalink.toffee.androidSimpleTooltip

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff.Mode.SRC_OUT
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.view.View
import com.banglalink.toffee.tooltip.R

/**
 * View que faz o efeito de escurecer a tela e dar destaque no ponto de ancoragem.<br></br>
 * Implementação baseada na resposta: http://stackoverflow.com/a/34702884/2826279
 *
 *
 * Created by douglas on 09/05/16.
 */
@SuppressLint("ViewConstructor")
class OverlayView internal constructor(
    context: Context?,
    private var mAnchorView: View,
    private val highlightShape: Int,
    private val mOffset: Float,
    private val overlayWindowBackground: Int,
    private val cornerRadius: Float
) : View(context) {
    private var bitmap: Bitmap? = null
    private var invalidated = true
    override fun dispatchDraw(canvas: Canvas) {
        if (invalidated || bitmap == null || bitmap!!.isRecycled) createWindowFrame()
        // The bitmap is checked again because of Android memory cleanup behavior. (See #42)
        if (bitmap != null && !bitmap!!.isRecycled) canvas.drawBitmap(bitmap!!, 0f, 0f, null)
    }
    
    private fun createWindowFrame() {
        val width = measuredWidth
        val height = measuredHeight
        if (width <= 0 || height <= 0) return
        if (bitmap != null && !bitmap!!.isRecycled) bitmap!!.recycle()
        bitmap = Bitmap.createBitmap(width, height, ARGB_8888)
        val osCanvas = Canvas(bitmap!!)
        val outerRectangle = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = overlayWindowBackground
        paint.isAntiAlias = true
        paint.alpha = resources.getInteger(mDefaultOverlayAlphaRes)
        osCanvas.drawRect(outerRectangle, paint)
        paint.color = Color.TRANSPARENT
        paint.xfermode = PorterDuffXfermode(SRC_OUT)
        val anchorRecr = SimpleTooltipUtils.calculateRectInWindow(mAnchorView)
        val overlayRecr = SimpleTooltipUtils.calculateRectInWindow(this)
        val left = anchorRecr.left - overlayRecr.left
        val top = anchorRecr.top - overlayRecr.top
        val rect =
            RectF(left - mOffset, top - mOffset, left + mAnchorView.measuredWidth + mOffset, top + mAnchorView.measuredHeight + mOffset)
        if (highlightShape == HIGHLIGHT_SHAPE_RECTANGULAR) {
            osCanvas.drawRect(rect, paint)
        } else if (highlightShape == HIGHLIGHT_SHAPE_RECTANGULAR_ROUNDED) {
            osCanvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        } else {
            osCanvas.drawOval(rect, paint)
        }
        invalidated = false
    }
    
    override fun isInEditMode(): Boolean {
        return true
    }
    
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        invalidated = true
    }
    
    var anchorView: View
        get() = mAnchorView
        set(anchorView) {
            mAnchorView = anchorView
            invalidate()
        }
    
    companion object {
        const val HIGHLIGHT_SHAPE_OVAL = 0
        const val HIGHLIGHT_SHAPE_RECTANGULAR = 1
        const val HIGHLIGHT_SHAPE_RECTANGULAR_ROUNDED = 2
        private val mDefaultOverlayAlphaRes: Int = R.integer.simpletooltip_overlay_alpha
    }
}