package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KabbikTopBannerApiResponse(
    @SerialName("data" ) var bannerItems : List<KabbikItem> = listOf()
): ExternalBaseResponse()
