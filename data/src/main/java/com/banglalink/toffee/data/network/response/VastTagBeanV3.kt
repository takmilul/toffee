package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.NativeAdSettings
import com.banglalink.toffee.model.VastTagV3
import com.google.gson.annotations.SerializedName

data class VastTagBeanV3(
    @SerializedName("vastTags")
    val vastTagV3: List<VastTagV3>? = null,
    @SerializedName("nativeAdSettings")
    val nativeAdSettings: List<NativeAdSettings>? = null
)