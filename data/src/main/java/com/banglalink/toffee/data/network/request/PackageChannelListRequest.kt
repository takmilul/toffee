package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class PackageChannelListRequest(
    @SerializedName("packageId")
    val packageId: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String,
    @SerializedName("offset")
    val offset: Int = 0,
    @SerializedName("limit")
    val limit: Int = 100
) : BaseRequest("getPackageDetailsV2")