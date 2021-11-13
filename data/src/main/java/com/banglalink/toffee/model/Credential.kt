package com.banglalink.toffee.model

import com.banglalink.toffee.data.network.response.BodyResponse
import com.google.gson.annotations.SerializedName

class Credential : BodyResponse() {
    @SerializedName("password")
    var password: String? = null
    @SerializedName("systemTime")
    var systemTime: String? = null
    @SerializedName("customerId")
    var customerId: Int = 0
    @SerializedName("isVerifiedUser")
    var isVerifiedUser: Boolean? = false
}