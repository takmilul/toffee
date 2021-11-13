package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelRatingBean (
    @SerializedName("isRated")
    val isRated: Int,
    @SerializedName("ratingCount")
    val ratingCount: Float,
    @SerializedName("message")
    val message: String,
    @SerializedName("messageType")
    val messageType: String,
    @SerializedName("systemTime")
    val systemTime: String
)