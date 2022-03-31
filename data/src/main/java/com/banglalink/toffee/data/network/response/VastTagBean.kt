package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.NativeAdTag
import com.banglalink.toffee.model.VastTag

import com.google.gson.annotations.SerializedName

class VastTagBean(
    @SerializedName("numOfTags")
    val numOfTags: Int = 0,
    @SerializedName("linearTags")
    val liveTags: List<VastTag>?,
    @SerializedName("vodTags")
    val vodTags: List<VastTag>?,
    @SerializedName("stingrayTags")
    val stingrayTags: List<VastTag>?,
    @SerializedName("nativeAdsTags")
    val nativeAdsTags:NativeAdTag?
)