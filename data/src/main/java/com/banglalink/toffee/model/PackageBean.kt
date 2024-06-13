package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PackageBean(
    @SerialName("subscribedPackages")
    val packageList: List<Package>? = null
)