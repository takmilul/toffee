package com.banglalink.toffee.ui.category.movie

import android.view.View
import androidx.viewpager2.widget.ViewPager2

private const val MAX_SCALE = 1f
private const val MIN_SCALE = 0.7f
private const val DIFF_SCALE = MAX_SCALE - MIN_SCALE

class ZoomInPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height
            val scale = MAX_SCALE
            when {
                position < -1 -> {
                    scaleX = scale + position * DIFF_SCALE
                    scaleY = MIN_SCALE
                }
                position <= 1 -> {
                    /*val scaleFactor = max(MIN_SCALE, 1 - abs(position))
                    val verticalMargin = pageHeight * (1 - scaleFactor) / 2
                    val horizontalMargin = pageWidth * (1 - scaleFactor) / 2
                    translationX = if (position < 0) {
                        horizontalMargin - verticalMargin / 2
                    } else {
                        horizontalMargin + verticalMargin / 2
                    }*/
                    scaleX = scale - position * DIFF_SCALE
                    scaleY = MAX_SCALE
                }
                else -> {
                    scaleX = scale + position * DIFF_SCALE
                    scaleY = MIN_SCALE
                }
            }
        }
    }
}