package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KeepAliveRequest(
    @SerialName("contentId")
    val contentId: Int,
    @SerialName("contentType")
    val contentType: String,
    @SerialName("data_source")
    val dataSource: String? = "iptv_programs",
    @SerialName("channel_owner_id")
    val ownerId: Int = 0,
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("lat")
    val lat: String,
    @SerialName("lon")
    val lon: String,
    @SerialName("isNetworkSwitch")
    val isNetworkSwitch: Boolean = false,
    @SerialName("type")
    val type: String = "FOREGROUND"
) : BaseRequest(ApiNames.SEND_KEEP_ALIVE)