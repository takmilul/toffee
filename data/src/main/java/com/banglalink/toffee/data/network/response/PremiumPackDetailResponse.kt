package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ChannelInfo
import com.google.gson.annotations.SerializedName

data class PremiumPackDetailResponse(
    @SerializedName("response")
    val response: PremiumPackDetailBean? = PremiumPackDetailBean()
): BaseResponse()

data class PremiumPackDetailBean(
    @SerializedName("LIVE")
    val linearChannelList: List<ChannelInfo>? = listOf(),
    @SerializedName("VOD")
    val vodChannelList: List<ChannelInfo>? = listOf(),
    @SerializedName("totalCount")
    val totalCount: Int = 0
)