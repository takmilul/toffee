package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.LoginByPhoneBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginByPhoneResponse(
    @SerialName("response")
    val response: LoginByPhoneBean?=null
) : BaseResponse()