package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PremiumPackStatusRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("pack_id")
    val packId: Int,
    @SerialName("is_prepaid")
    val isPrepaid: Int
) : BaseRequest(ApiNames.PREMIUM_DATA_PACK_STATUS)