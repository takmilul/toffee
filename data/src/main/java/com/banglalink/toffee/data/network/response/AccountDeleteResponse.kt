package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.AccountDeleteBean
import com.banglalink.toffee.model.LogoutBean
import com.google.gson.annotations.SerializedName

data class AccountDeleteResponse(
    @SerializedName("response")
    val response: AccountDeleteBean
) : BaseResponse()