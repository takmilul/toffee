package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class SubCategoryRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest("getUgcSubCategories")
