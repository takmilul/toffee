package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    @SerialName("fullname")
    val fullname: String,
    @SerialName("email")
    val email: String,
    @SerialName("phoneNo")
    val phoneNo: String,
    @SerialName("address")
    val address: String,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String
) : BaseRequest(ApiNames.UPDATE_USER_PROFILE)