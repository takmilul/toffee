package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import com.google.gson.annotations.SerializedName

data class PackVoucherMethodRequest (
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("voucher")
    val voucher: String,
    @SerializedName("pack_name")
    val packName: String
) : BaseRequest(ApiNames.CHECK_VOUCHER_STATUS)