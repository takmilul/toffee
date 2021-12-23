package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName


data class ViewingContentRequest(
    @SerializedName("type")
    val type: String,
    @SerializedName("contentId")
    val contentId: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lon")
    val lon: String
) : BaseRequest(ApiNames.SEND_VIEWING_CONTENT)