package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class FireworkRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password:String,
    @SerializedName("type")
    val type:String="Live"
) : BaseRequest(ApiNames.GET_FIREWORK_LIST)