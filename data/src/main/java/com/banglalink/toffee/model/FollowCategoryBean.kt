package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class FollowCategoryBean(
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("isFollowed")
    val isFollowed: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("messageType")
    val messageType: String,
    @SerializedName("systemTime")
    val systemTime: String
)