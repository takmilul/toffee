package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class DBVersionV2(
    @SerialName(value = "api_name")
    val apiName: String,
    @SerialName(value = "db_version")
    val dbVersion: Int
) : Parcelable