package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RechargeByBkashRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("payment_method_id")
    val paymentMethodId: Int,
    @SerialName("pack_id")
    val packId: Int,
    @SerialName("pack_title")
    val packTitle: String?,
    @SerialName("data_pack_id")
    val dataPackId: Int,
    @SerialName("data_pack_code")
    val dataPackCode: String?,
    @SerialName("data_pack_details")
    val dataPackDetail: String?,
    @SerialName("data_pack_price")
    val dataPackPrice: Int,
    @SerialName("is_prepaid")
    val isPrepaid: Int,
) : BaseRequest(ApiNames.RECHARGE_BY_BKASH)