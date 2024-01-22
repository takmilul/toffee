package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackageDetails(
    @SerialName("id")
    val id: Int,
    @SerialName("package_name")
    val packageName: String,
    @SerialName("price")
    val price: Int,
    @SerialName("programs")
    val programs: List<ChannelInfo>
)