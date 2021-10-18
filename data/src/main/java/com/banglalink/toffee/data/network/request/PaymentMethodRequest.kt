package com.banglalink.toffee.data.network.request

data class PaymentMethodRequest(
    val customerId: Int,
    val password: String
) : BaseRequest("getUgcPaymentMethodList")