package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class UgcUserChannelBean(
    @SerializedName("channels")
    val channels: List<UgcUserChannelInfo>?,
    val count: Int,
    val totalCount: Int = 0,
    val systemTime: String?=null
)