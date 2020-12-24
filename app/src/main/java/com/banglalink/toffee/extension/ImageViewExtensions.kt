package com.banglalink.toffee.extension

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.R

fun ImageView.loadProfileImage(imageUrl: String) {

    if (imageUrl.isBlank()) {
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_menu_profile))
    } else {
        load(imageUrl) {
            crossfade(true)
            transformations(CircleCropTransformation())
            placeholder(R.drawable.ic_menu_profile)
            error(R.drawable.ic_menu_profile)
        }
    }
}

fun ImageView.loadBase64(data: String) {
    try {
        val imageBytes = Base64.decode(data, Base64.NO_WRAP)
        val bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        setImageBitmap(bmp)
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}