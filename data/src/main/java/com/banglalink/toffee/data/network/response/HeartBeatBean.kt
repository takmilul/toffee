package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.DBVersion
import com.banglalink.toffee.model.DBVersionV2
import com.google.gson.annotations.SerializedName

data class HeartBeatBean(
    @SerializedName("mqttIsActive")
    val mqttIsActive: Int,
    @SerializedName("systemTime")
    val systemTime: String? = null,
    @SerializedName("sessionToken")
    val sessionToken: String? = null,
    @SerializedName("headerSessionToken")
    val headerSessionToken: String? = null,
    @SerializedName("dbVersion")
    var dbVersion: DBVersion? = null,
    @SerializedName("dbVersionV2")
    var dbVersionList: List<DBVersionV2>? = null
): BaseResponse()