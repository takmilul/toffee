package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class FollowCategoryRequest(
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("isFollowed")
    val isFollowed: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest("ugcFollowOnCategory")