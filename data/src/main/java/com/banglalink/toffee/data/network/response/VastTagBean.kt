package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.VastTag
import com.google.gson.annotations.SerializedName

class VastTagBean (
    @SerializedName("numOfTags")
    val numOfTags: Int = 0,
    @SerializedName("tags")
    val tags: List<VastTag>?
)