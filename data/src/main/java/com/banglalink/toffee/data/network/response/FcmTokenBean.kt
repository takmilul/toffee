package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

class FcmTokenBean(
    @SerializedName("message")
    val message: String
)