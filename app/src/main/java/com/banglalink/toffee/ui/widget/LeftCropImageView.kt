package com.banglalink.toffee.ui.widget

import android.content.Context
import android.util.AttributeSet

class LeftCropImageView @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): androidx.appcompat.widget.AppCompatImageView(mContext, attrs, defStyle) {
    
    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val matrix = imageMatrix
        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight
        val scaleRatio = 1.2
        matrix.postTranslate((measuredWidth - imageWidth).toFloat(), 1f)
//        matrix.postScale(-0.2f, 1f)
        imageMatrix = matrix
        return super.setFrame(l, t, r, b)
    }
}