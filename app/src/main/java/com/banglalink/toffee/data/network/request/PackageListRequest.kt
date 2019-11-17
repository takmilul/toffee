package com.banglalink.toffee.data.network.request

data class PackageListRequest(
    val customerId: Int,
    val password: String,
    val offset: Int = 0,
    val limit: Int = 100,
    val deviceType: Int = 1
) : BaseRequest("getSubscribedPackagesV2")