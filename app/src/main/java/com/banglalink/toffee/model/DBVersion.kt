package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class DBVersion(
    @SerializedName(value = "channel_db_version",alternate = ["chanelDbVersion"])
    val chanelDbVersion: Int,
    @SerializedName(value = "vod_db_version",alternate = ["vodDbVersion"])
    val vodDbVersion: Int,
    @SerializedName("notification_db_version",alternate = ["notificationDbVersion"])
    val notificationDbVersion: Int,
    @SerializedName(value = "catchup_db_version",alternate = ["catchupDbVersion"])
    val catchupDbVersion: Int,
    @SerializedName(value = "package_db_version",alternate = ["packageDbVersion"])
    val packageDbVersion: Int,
    @SerializedName("category_db_version",alternate = ["categoryDbVersion"])
    val categoryDbVersion:Int
)