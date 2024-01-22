package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class KabbikTopBannerApiResponse(
    @SerializedName("data" ) var bannerItems : List<KabbikItem> = listOf()
): ExternalBaseResponse()
