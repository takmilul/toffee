package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
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
) : BaseRequest(ApiNames.GET_APP_HOME_PAGE_CONTENT_V2)