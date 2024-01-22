package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryRequest(
    @SerialName("telcoId")
    val telcoId: Int = 1
) : BaseRequest(ApiNames.GET_CATEGORIES)