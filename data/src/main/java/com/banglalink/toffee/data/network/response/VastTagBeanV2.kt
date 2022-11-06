package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.NativeAdSettings
import com.banglalink.toffee.model.VastTagV2
import com.google.gson.annotations.SerializedName

data class VastTagBeanV2 (
    @SerializedName("numOfTags")
    val numOfTags: Int = 0,
    @SerializedName("linearTags")
    val liveTags: List<VastTagV2>?,
    @SerializedName("vodTags")
    val vodTags: List<VastTagV2>?,
    @SerializedName("stingrayTags")
    val stingrayTags: List<VastTagV2>?,
    @SerializedName("nativeAdSettings")
    val nativeAdSettings: List<NativeAdSettings>? = null
)