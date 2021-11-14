package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class AllChannelRequest(
    @SerializedName("subCategoryId")
    val subCategoryId: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("telcoId")
    val telcoId: Int = 1,
    @SerializedName("limit")
    val limit: Int = 200
) : BaseRequest("getUgcAppHomePageContentTofeeV2")