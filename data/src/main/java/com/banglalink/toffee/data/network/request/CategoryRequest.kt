package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class CategoryRequest(
    @SerializedName("telcoId")
    val telcoId: Int = 1
) : BaseRequest("getUgcCategories")