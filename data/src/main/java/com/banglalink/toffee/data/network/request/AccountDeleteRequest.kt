package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountDeleteRequest(
    @SerialName("msisdn")
    val msisdn: String?,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
) : BaseRequest(ApiNames.ACCOUNT_DELETE)