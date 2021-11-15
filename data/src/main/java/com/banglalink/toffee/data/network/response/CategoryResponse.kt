package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.CategoryBean
import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("response")
    val response: CategoryBean
) : BaseResponse()