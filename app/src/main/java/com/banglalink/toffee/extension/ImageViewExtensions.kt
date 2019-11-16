package com.banglalink.toffee.extension

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