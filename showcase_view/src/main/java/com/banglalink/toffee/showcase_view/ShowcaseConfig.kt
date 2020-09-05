package com.banglalink.toffee.showcase_view

import android.graphics.Color
import android.graphics.Typeface
import com.banglalink.toffee.showcase_view.shape.Shape

class ShowcaseConfig {
    var delay: Long = -1
    var maskColor: Int
    var dismissTextStyle: Typeface? = null
    var contentTextColor: Int
    var dismissTextColor: Int
    var fadeDuration: Long = -1
    var shape: Shape? = null
    var shapePadding = -1
    var renderOverNavigationBar: Boolean? = null
        private set
    
    fun setRenderOverNavigationBar(renderOverNav: Boolean) {
        renderOverNavigationBar = renderOverNav
    }
    
    companion object {
        const val DEFAULT_MASK_COLOUR = "#dd335075"
    }
    
    init {
        maskColor = Color.parseColor(DEFAULT_MASK_COLOUR)
        contentTextColor = Color.parseColor("#ffffff")
        dismissTextColor = Color.parseColor("#ffffff")
    }
}