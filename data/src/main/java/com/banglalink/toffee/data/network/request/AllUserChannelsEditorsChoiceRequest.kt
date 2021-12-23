package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class AllUserChannelsEditorsChoiceRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest(ApiNames.GET_EDITOR_CHOICE)