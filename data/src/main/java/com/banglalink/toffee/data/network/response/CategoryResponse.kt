package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.CategoryBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    @SerialName("response")
    val response: CategoryBean
) : BaseResponse()