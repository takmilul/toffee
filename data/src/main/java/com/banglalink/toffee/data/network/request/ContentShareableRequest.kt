package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class ContentShareableRequest(
    @SerializedName("videoShareUrl")
    val videoShareUrl: String,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("telcoId")
    val telcoId: Int = 1
) : BaseRequest(ApiNames.GET_CONTENT_FROM_SEARCHABLE_URL)