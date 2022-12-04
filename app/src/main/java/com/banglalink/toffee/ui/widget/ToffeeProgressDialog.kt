package com.banglalink.toffee.ui.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.GifLayoutNewBinding

class ToffeeProgressDialog(context: Context) : Dialog(context, R.style.TransparentProgressDialog) {

    init {
        val windowManger = window!!.attributes
        windowManger.gravity = Gravity.CENTER_HORIZONTAL
        window!!.attributes = windowManger
        setTitle(null)
        setCancelable(false)
        setOnCancelListener(null)
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        val binding = GifLayoutNewBinding.inflate(this.layoutInflater)
        binding.progressBar.load(R.drawable.screen_loader)
        layout.addView(binding.root, params)
        addContentView(layout, params)
    }
}