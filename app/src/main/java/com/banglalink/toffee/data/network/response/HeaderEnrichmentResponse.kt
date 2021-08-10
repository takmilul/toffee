package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class HeaderEnrichmentResponse (
    @SerializedName("msisdn")
    val phoneNumber: String,
    @SerializedName("is_bl")
    val isBanglalinkNumber: Boolean,
): BaseResponse()