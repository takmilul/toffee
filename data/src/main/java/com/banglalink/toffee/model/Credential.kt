package com.banglalink.toffee.model

import com.banglalink.toffee.data.network.response.BodyResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Credential : BodyResponse() {
    @SerialName("password")
    var password: String? = null
    @SerialName("systemTime")
    var systemTime: String? = null
    @SerialName("customerId")
    var customerId: Int = 0
    @SerialName("isVerifiedUser")
    var isVerifiedUser: Boolean? = false
}