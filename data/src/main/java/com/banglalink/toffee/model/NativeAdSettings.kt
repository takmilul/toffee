package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class NativeAdSettings(
    @SerializedName("area")
    val area: Int = 0,
    @SerializedName("isActive")
    val isActive: Boolean = false,
    @SerializedName("adUnitId")
    val adUnitId: String? = null,
    @SerializedName("adInterval")
    val adInterval: Int = 4,
)