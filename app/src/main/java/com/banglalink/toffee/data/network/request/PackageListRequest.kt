package com.banglalink.toffee.data.network.request

data class PackageListRequest(
    val customerId: Int,
    val password: String,
    override val offset: Int = 0,
    override val limit: Int = 100
) : BasePagingRequest("getPackagesWithSubscription")