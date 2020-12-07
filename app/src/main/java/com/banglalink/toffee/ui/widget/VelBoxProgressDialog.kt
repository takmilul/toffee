package com.banglalink.toffee.ui.widget

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.banglalink.toffee.R


class VelBoxProgressDialog(context: Context) : Dialog(context, R.style.TransparentProgressDialog) {

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

        layout.addView(LayoutInflater.from(context).inflate(R.layout.gif_layout_new, null), params)
        addContentView(layout, params)
    }

}