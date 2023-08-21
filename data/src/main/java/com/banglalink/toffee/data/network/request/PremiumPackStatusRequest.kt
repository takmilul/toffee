package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class PremiumPackStatusRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("pack_id")
    val packId: Int,
    @SerializedName("is_prepaid")
    val isPrepaid: Int
) : BaseRequest(ApiNames.PREMIUM_DATA_PACK_STATUS)