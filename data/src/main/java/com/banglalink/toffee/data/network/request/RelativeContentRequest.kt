package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class RelativeContentRequest(
    @SerializedName("contentId")
    val contentId: String,
    @SerializedName("videoTag")
    val videoTag: String,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("categoryId")
    val categoryId: Int,
    @SerializedName("subCategoryId")
    val subCategoryId: Int,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("limit")
    val limit: Int = 30
) : BaseRequest("getUgcRelativeContentsExt")