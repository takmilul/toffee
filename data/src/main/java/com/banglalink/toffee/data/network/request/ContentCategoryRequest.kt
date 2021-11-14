package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class ContentCategoryRequest(
    @SerializedName("telcoId")
    val telcoId: Int = 1
) : BaseRequest("getUgcActiveInactiveCategories")