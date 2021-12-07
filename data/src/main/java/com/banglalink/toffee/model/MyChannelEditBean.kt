package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelEditBean (
    @SerializedName("profileImage")
    val profileImage: String?,
    @SerializedName("bannerImage")
    val bannerImage: String?,
    @SerializedName("systemTime")
    val systemTime: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("messageType")
    val messageType: String?
)