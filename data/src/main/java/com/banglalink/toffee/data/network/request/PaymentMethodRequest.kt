package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class PaymentMethodRequest(
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest("getUgcPaymentMethodList")