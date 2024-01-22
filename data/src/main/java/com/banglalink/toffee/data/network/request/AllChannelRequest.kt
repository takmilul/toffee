package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AllChannelRequest(
    @SerialName("subCategoryId")
    val subCategoryId: Int,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("telcoId")
    val telcoId: Int = 1,
    @SerialName("limit")
    val limit: Int = 200
) : BaseRequest(ApiNames.GET_ALL_TV_CHANNELS)