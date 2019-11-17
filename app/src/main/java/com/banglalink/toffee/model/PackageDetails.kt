package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class PackageDetails(
    val id: Int,
    @SerializedName("package_name")
    val packageName: String,
    val price: Int,
    val programs: List<ChannelInfo>
)