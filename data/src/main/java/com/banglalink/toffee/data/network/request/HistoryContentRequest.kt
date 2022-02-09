package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class HistoryContentRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("limit")
    val limit: Int = 30
) : BaseRequest(ApiNames.GET_HISTORY_CONTENTS)