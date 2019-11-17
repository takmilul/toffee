package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

class PackageBean(
    @SerializedName("subscribedPackages")
    val packageList: List<Package>,
//    val systemTime: String,
    val balance: Int
)