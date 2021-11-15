package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.LoginByPhoneBean
import com.google.gson.annotations.SerializedName

data class LoginByPhoneResponse(
    @SerializedName("response")
    val response: LoginByPhoneBean
) : BaseResponse()