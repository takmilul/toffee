package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class TermsConditionRequest(
    @SerializedName("customerId")
    var customerId: Int,
    @SerializedName("password")
    var password: String
) : BaseRequest("getUgcTermsAndConditions")