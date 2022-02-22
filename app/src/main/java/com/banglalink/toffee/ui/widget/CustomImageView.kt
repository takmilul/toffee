package com.banglalink.toffee.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.banglalink.toffee.R

class CustomImageView : ImageView {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        val height = width * 9 / 16
        setMeasuredDimension(width, height)
        scaleType=ScaleType.CENTER_INSIDE
        background=ContextCompat.getDrawable(context, R.color.fw_gnt_black)
    }
}