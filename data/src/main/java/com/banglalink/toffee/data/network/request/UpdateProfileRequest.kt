package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("fullname")
    val fullname: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phoneNo")
    val phoneNo: String,
    @SerializedName("address")
    val address: String,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest(ApiNames.UPDATE_USER_PROFILE)