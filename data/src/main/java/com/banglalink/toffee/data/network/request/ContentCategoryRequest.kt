package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class ContentCategoryRequest(
    @SerializedName("telcoId")
    val telcoId: Int = 1
) : BaseRequest(ApiNames.GET_ACTIVE_INACTIVE_CATEGORIES)