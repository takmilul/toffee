package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.FollowCategoryBean
import com.google.gson.annotations.SerializedName

data class FollowCategoryResponse(
    @SerializedName("response")
    val response: FollowCategoryBean
) : BaseResponse()