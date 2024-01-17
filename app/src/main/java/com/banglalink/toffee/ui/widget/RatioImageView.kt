package com.banglalink.toffee.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.px
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily

class RatioImageView(
    context: Context,
    attrs: AttributeSet?,
) : ShapeableImageView(context, attrs) {
    private var aspectRatio: String? = "16:9"
    private var cornerRadius: Float = 0f
    
    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RatioImageView)
        aspectRatio = typedArray.getString(R.styleable.RatioImageView_aspectRatio)
        cornerRadius = typedArray.getDimension(R.styleable.RatioImageView_cornerRadius, 0f)
        typedArray.recycle()
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = measuredHeight
        val width = ((height * 3) / 4.5).toInt()
        setMeasuredDimension(width, height)
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
    }
    
    private val globalLayoutListener = OnGlobalLayoutListener {
        shapeAppearanceModel = shapeAppearanceModel
            .toBuilder()
            .setAllCorners(CornerFamily.ROUNDED, cornerRadius.px)
            .build()
    }
}