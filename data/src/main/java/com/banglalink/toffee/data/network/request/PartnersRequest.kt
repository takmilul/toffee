package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class PartnersRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
) : BaseRequest("getUgcPartnerList")