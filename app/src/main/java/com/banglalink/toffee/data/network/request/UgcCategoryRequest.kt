package com.banglalink.toffee.data.network.request

data class UgcCategoryRequest(
    val telcoId: Int = 1
) : BaseRequest("getUgcCategories")