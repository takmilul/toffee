package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.NavCategoryBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NavCategoryResponse(
    @SerialName("response")
    val response: NavCategoryBean? = null
) : BaseResponse()