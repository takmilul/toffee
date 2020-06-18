package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class SubscriberPhotoBean(
    @SerializedName("message")
    val message:String?,
    @SerializedName("user_photo")
    val userPhoto:String?)