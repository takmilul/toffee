package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeatureContentRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String
) : BaseRequest(ApiNames.GET_FEATURED_CONTENTS)