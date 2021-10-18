package com.banglalink.toffee.data.network.request

data class CategoryRequest(
    val telcoId: Int = 1
) : BaseRequest("getUgcCategories")