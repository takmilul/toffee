package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class PackageDetailsBean(
    @SerializedName("package")
    val packageDetails: PackageDetails
)