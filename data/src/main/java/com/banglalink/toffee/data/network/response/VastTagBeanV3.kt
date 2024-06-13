package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.NativeAdSettings
import com.banglalink.toffee.model.VastTagV3
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VastTagBeanV3(
    @SerialName("vastTags")
    val vastTagV3: List<VastTagV3>? = null,
    @SerialName("nativeAdSettings")
    val nativeAdSettings: List<NativeAdSettings>? = null
)