package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.DBVersionV2
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeartBeatBean(
    @SerialName("mqttIsActive")
    val mqttIsActive: Int,
    @SerialName("systemTime")
    val systemTime: String? = null,
    @SerialName("sessionToken")
    val sessionToken: String? = null,
    @SerialName("headerSessionToken")
    val headerSessionToken: String? = null,
    @SerialName("dbVersion")
    var dbVersionList: List<DBVersionV2>? = null
): BaseResponse()