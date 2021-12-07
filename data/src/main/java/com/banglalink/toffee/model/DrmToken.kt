package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class DrmToken(
    @SerializedName("drmToken")
    val drmToken: String
)