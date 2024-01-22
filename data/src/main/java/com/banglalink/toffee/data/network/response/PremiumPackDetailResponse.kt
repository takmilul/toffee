package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ChannelInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PremiumPackDetailResponse(
    @SerialName("response")
    val response: PremiumPackDetailBean? = PremiumPackDetailBean(),
) : BaseResponse()

@Serializable
data class PremiumPackDetailBean(
    @SerialName("LIVE")
    val linearChannelList: List<ChannelInfo>? = listOf(),
    @SerialName("VOD")
    val vodChannelList: List<ChannelInfo>? = listOf(),
    @SerialName("totalCount")
    val totalCount: Int = 0,
)