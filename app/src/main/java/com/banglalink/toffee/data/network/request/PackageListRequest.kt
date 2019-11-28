package com.banglalink.toffee.data.network.request

data class PackageListRequest(
    val customerId: Int,
    val password: String,
    val offset: Int = 0,
    val limit: Int = 100
) : BaseRequest("getSubscribedPackagesV2")