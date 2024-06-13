package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ViewingContentRequest(
    @SerialName("type")
    val type: String,
    @SerialName("contentId")
    val contentId: Int,
    @SerialName("data_source")
    val dataSource: String? = "iptv_programs",
    @SerialName("channel_owner_id")
    val ownerId: String? = "0",
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("lat")
    val lat: String,
    @SerialName("lon")
    val lon: String
) : BaseRequest(ApiNames.SEND_VIEWING_CONTENT)