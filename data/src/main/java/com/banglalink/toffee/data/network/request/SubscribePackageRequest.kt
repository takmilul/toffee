package com.banglalink.toffee.data.network.request

data class SubscribePackageRequest(
    val packageId:Int,
    val customerId: Int,
    val password: String,
    val isAutoRenew:String = "false"
) : BaseRequest("subscribeAPackage")