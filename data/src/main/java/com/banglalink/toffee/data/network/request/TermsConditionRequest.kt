package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TermsConditionRequest(
    @SerialName("customerId")
    var customerId: Int,
    @SerialName("password")
    var password: String
) : BaseRequest(ApiNames.GET_TERMS_AND_CONDITIONS)