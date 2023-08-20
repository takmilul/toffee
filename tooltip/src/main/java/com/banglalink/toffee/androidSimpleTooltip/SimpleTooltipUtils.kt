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

import android.content.Context
import android.content.res.Resources
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes

/**
 * SimpleTooltipUtils
 * Created by douglas on 09/05/16.
 */
@Suppress("unused")
object SimpleTooltipUtils {
    
    @JvmStatic
    fun calculateRectOnScreen(view: View): RectF {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return RectF(
            location[0].toFloat(),
            location[1].toFloat(),
            (location[0] + view.measuredWidth).toFloat(),
            (location[1] + view.measuredHeight).toFloat()
        )
    }
    
    @JvmStatic
    fun calculateRectInWindow(view: View): RectF {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        return RectF(
            location[0].toFloat(),
            location[1].toFloat(),
            (location[0] + view.measuredWidth).toFloat(),
            (location[1] + view.measuredHeight).toFloat()
        )
    }
    
    fun dpFromPx(px: Float): Float {
        return px / Resources.getSystem().displayMetrics.density
    }
    
    @JvmStatic
    fun pxFromDp(dp: Float): Float {
        return dp * Resources.getSystem().displayMetrics.density
    }
    
    @JvmStatic
    fun setWidth(view: View, width: Float) {
        var params = view.layoutParams
        if (params == null) {
            params = LayoutParams(width.toInt(), view.height)
        } else {
            params.width = width.toInt()
        }
        view.layoutParams = params
    }
    
    @JvmStatic
    fun tooltipGravityToArrowDirection(tooltipGravity: Int): Int {
        return when (tooltipGravity) {
            Gravity.START -> ArrowDrawable.RIGHT
            Gravity.END -> ArrowDrawable.LEFT
            Gravity.TOP -> ArrowDrawable.BOTTOM
            Gravity.BOTTOM -> ArrowDrawable.TOP
            Gravity.CENTER -> ArrowDrawable.TOP
            else -> throw IllegalArgumentException("Gravity must have be CENTER, START, END, TOP or BOTTOM.")
        }
    }
    
    @JvmStatic
    fun setX(view: View, x: Int) {
        view.x = x.toFloat()
    }
    
    @JvmStatic
    fun setY(view: View, y: Int) {
        view.y = y.toFloat()
    }
    
    private fun getOrCreateMarginLayoutParams(view: View): MarginLayoutParams {
        val lp = view.layoutParams
        return if (lp != null) {
            if (lp is MarginLayoutParams) {
                lp
            } else {
                MarginLayoutParams(lp)
            }
        } else {
            MarginLayoutParams(view.width, view.height)
        }
    }
    
    @JvmStatic
    fun removeOnGlobalLayoutListener(view: View, listener: OnGlobalLayoutListener?) {
        view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }
    
    @JvmStatic
    fun setTextAppearance(tv: TextView, @StyleRes textAppearanceRes: Int) {
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            tv.setTextAppearance(textAppearanceRes)
        } else {
            tv.setTextAppearance(tv.context, textAppearanceRes)
        }
    }
    
    @JvmStatic
    fun getColor(context: Context, @ColorRes colorRes: Int): Int {
        return if (VERSION.SDK_INT >= VERSION_CODES.M) {
            context.getColor(colorRes)
        } else {
            context.resources.getColor(colorRes)
        }
    }
    
    @JvmStatic
    fun getDrawable(context: Context, @DrawableRes drawableRes: Int): Drawable? {
        return if (VERSION.SDK_INT >= VERSION_CODES.M) {
            context.getDrawable(drawableRes)
        } else {
            context.resources.getDrawable(drawableRes)
        }
    }
    
    /**
     * Verify if the first child of the rootView is a FrameLayout.
     * Used for cases where the Tooltip is created inside a Dialog or DialogFragment.
     *
     * @param anchorView
     * @return FrameLayout or anchorView.getRootView()
     */
    @JvmStatic
    fun findFrameLayout(anchorView: View): ViewGroup {
        var rootView = anchorView.rootView as ViewGroup
        if (rootView.childCount == 1 && rootView.getChildAt(0) is FrameLayout) {
            rootView = rootView.getChildAt(0) as ViewGroup
        }
        return rootView
    }
}