package com.banglalink.toffee.ui.player

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PlayerPreview @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defAttrStyle: Int = 0)
    : View(context, attrs, defAttrStyle) {
    private lateinit var controller: ExoMediaController4

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if(this.parent.parent is ExoMediaController4) {
            controller = this.parent.parent as ExoMediaController4
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return controller.onPreviewTouch(event) || super.onTouchEvent(event)
    }
}