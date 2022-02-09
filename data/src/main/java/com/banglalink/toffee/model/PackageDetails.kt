package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class PackageDetails(
    @SerializedName("id")
    val id: Int,
    @SerializedName("package_name")
    val packageName: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("programs")
    val programs: List<ChannelInfo>
)