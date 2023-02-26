package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class PremiumPackListResponse(
    @SerializedName("response")
    val response: PremiumPackListBean? = PremiumPackListBean()
) : BaseResponse()

data class PremiumPackListBean(
    @SerializedName("packs")
    val premiumPacks: List<PremiumPack>? = listOf()
)

data class PremiumPack(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("pack_title")
    val packTitle: String? = null,
    @SerializedName("pack_poster_mobile")
    val packImage: String? = null,
    @SerializedName("pack_description")
    val packDescription: String? = null,
    @SerializedName("content_id")
    val contentId: List<Int> = listOf(),
    @SerializedName("isPurchaseAvailable")
    val isPurchaseAvailable: Int = 0,
    @SerializedName("isAvailableFreePeriod")
    val isAvailableFreePeriod: Int = 0,
    @SerializedName("isPackPurchased")
    var isPackPurchased: Boolean = false,
    @SerializedName("expiryDate")
    var expiryDate: String
)