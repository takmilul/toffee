package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.AccountDeleteBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountDeleteResponse(
    @SerialName("response")
    val response: AccountDeleteBean
) : BaseResponse()