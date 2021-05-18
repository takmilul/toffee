package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DBVersionV2(
    @SerializedName(value = "api_name")
    val apiName: String,
    @SerializedName(value = "db_version")
    val dbVersion: Int
) : Parcelable