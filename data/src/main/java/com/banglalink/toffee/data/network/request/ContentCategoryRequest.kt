package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentCategoryRequest(
    @SerialName("telcoId")
    val telcoId: Int = 1
) : BaseRequest(ApiNames.GET_ACTIVE_INACTIVE_CATEGORIES)