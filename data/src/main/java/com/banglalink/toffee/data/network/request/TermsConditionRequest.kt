package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class TermsConditionRequest(
    @SerializedName("customerId")
    var customerId: Int,
    @SerializedName("password")
    var password: String
) : BaseRequest(ApiNames.GET_UGC_TERMS_AND_CONDITIONS)