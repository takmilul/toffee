package com.banglalink.toffee.data.network.request

data class ContentCategoryRequest(
    val telcoId: Int = 1
) : BaseRequest("getUgcActiveInactiveCategories")