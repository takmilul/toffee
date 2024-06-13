package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackageDetailsBean(
    @SerialName("package")
    val packageDetails: PackageDetails
)