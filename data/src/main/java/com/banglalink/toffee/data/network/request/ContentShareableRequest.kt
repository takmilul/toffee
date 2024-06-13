package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentShareableRequest(
    @SerialName("videoShareUrl")
    val videoShareUrl: String,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("type")
    val type: String? = null,
    @SerialName("telcoId")
    val telcoId: Int = 1,
) : BaseRequest(ApiNames.GET_CONTENT_FROM_SEARCHABLE_URL)