package com.banglalink.toffee.data.network.request

data class UgcFollowCategoryRequest(
    val categoryId: Int,
    val isFollowed: Int,
    val customerId: Int,
    val password: String
): BaseRequest("ugcFollowOnCategory")