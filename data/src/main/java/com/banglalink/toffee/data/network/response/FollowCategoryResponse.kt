package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.FollowCategoryBean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowCategoryResponse(
    @SerialName("response")
    val response: FollowCategoryBean
) : BaseResponse()