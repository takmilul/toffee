package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class LoginByPhoneRequest(
    @SerializedName("phoneNo")
    val phoneNo: String,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lon")
    val lon: String,
    @SerializedName("parentId")
    val parentId: Int = 1,
    @SerializedName("email")
    val email: String = "",
    @SerializedName("serviceOperatorType")
    val serviceOperatorType: String = "TELCO",
    @SerializedName("referralCode")
    val referralCode: String = ""
) : BaseRequest(ApiNames.RE_REGISTATION)