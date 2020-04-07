package com.banglalink.toffee.data.network.request

data class AutoRenewRequest(
    val packageId:Int,
    val customerId: Int,
    val password: String,
    val isAutoRenew:String
) : BaseRequest("setAutoRenew")