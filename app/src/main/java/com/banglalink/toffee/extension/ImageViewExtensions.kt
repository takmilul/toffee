package com.banglalink.toffee.extension

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView

fun ImageView.loadBase64(data: String) {
    try {
        val imageBytes = Base64.decode(data, Base64.NO_WRAP)
        val bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        setImageBitmap(bmp)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}