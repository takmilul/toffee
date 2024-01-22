package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackVoucherMethodRequest (
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("voucher")
    val voucher: String,
    @SerialName("pack_name")
    val packName: String
) : BaseRequest(ApiNames.CHECK_VOUCHER_STATUS)