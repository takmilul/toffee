package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.NavCategoryBean
import com.google.gson.annotations.SerializedName

data class NavCategoryResponse(
    @SerializedName("response")
    val response: NavCategoryBean
) : BaseResponse()