package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginByPhoneRequest(
    @SerialName("phoneNo")
    val phoneNo: String,
    @SerialName("lat")
    val lat: String,
    @SerialName("lon")
    val lon: String,
    @SerialName("parentId")
    val parentId: Int = 1,
    @SerialName("email")
    val email: String = "",
    @SerialName("serviceOperatorType")
    val serviceOperatorType: String = "TELCO",
    @SerialName("referralCode")
    val referralCode: String = ""
) : BaseRequest(ApiNames.LOGIN_BY_PHONE_NO)