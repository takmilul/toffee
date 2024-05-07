package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//@Serializable
//data class PremiumPackSubHistoryRequest(
//    @SerialName("customerId")
//    var customerId: Int? = null,
//    @SerialName("password")
//    var password: String? = null
//) : BaseRequest(ApiNames.PREMIUM_PACK_SUBSCRIPTION_HISTORY)

@Serializable
data class PremiumPackSubHistoryRequest(val customerId: Int? =null,
    var password:String?=null
):BaseRequest(ApiNames.PREMIUM_PACK_SUBSCRIPTION_HISTORY)