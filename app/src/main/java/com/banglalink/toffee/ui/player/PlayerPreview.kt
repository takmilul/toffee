package com.banglalink.toffee.ui.player

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView

class PlayerPreview(context: Context, attrs: AttributeSet): AppCompatImageView(context, attrs) {
    private lateinit var controller: ExoMediaController4

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if(this.parent.parent.parent is ExoMediaController4) {
            controller = this.parent.parent.parent as ExoMediaController4
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return controller.onPreviewTouch(event) || super.onTouchEvent(event)
    }
}