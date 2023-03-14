package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class RechargeByBkashRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("payment_method_id")
    val paymentMethodId: Int,
    @SerializedName("pack_id")
    val packId: Int,
    @SerializedName("pack_title")
    val packTitle: String?,
    @SerializedName("data_pack_id")
    val dataPackId: Int,
    @SerializedName("data_pack_code")
    val dataPackCode: String?,
    @SerializedName("data_pack_details")
    val dataPackDetail: String?,
    @SerializedName("data_pack_price")
    val dataPackPrice: Int,
) : BaseRequest(ApiNames.RECHARGE_BY_BKASH)