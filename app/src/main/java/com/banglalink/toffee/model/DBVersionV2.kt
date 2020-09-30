package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class DBVersionV2(
    @SerializedName(value = "api_name")
    val apiName: String,
    @SerializedName(value = "db_version")
    val dbVersion: Int
)