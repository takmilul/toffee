package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class AccountDeleteRequest(
    @SerializedName("msisdn")
    val msisdn: String?,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
) : BaseRequest(ApiNames.ACCOUNT_DELETE)