package com.banglalink.toffee.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class LeftCropImageView : ImageView {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}
    
    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val matrix = imageMatrix
        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight
        val scaleRatio = 1.2
        matrix.postTranslate((measuredWidth - imageWidth).toFloat(), 1f)
//                    matrix.postScale(-0.2f, 1f)
        imageMatrix = matrix
        return super.setFrame(l, t, r, b)
    }
}