package com.banglalink.toffee.extension

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import coil.api.load
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.R

fun ImageView.loadProfileImage(imageUrl: String) {

    if (imageUrl.isBlank()) {
        setImageDrawable(context.getDrawable(R.drawable.ic_profile_default))
    } else {
        load(imageUrl) {
            crossfade(true)
            transformations(CircleCropTransformation())
            placeholder(R.drawable.ic_profile_default)
            error(R.drawable.ic_profile_default)
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