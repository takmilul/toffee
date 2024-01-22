package com.banglalink.toffee.data.network.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PremiumPackListResponse(
    @SerialName("response")
    val response: PremiumPackListBean? = PremiumPackListBean(),
) : BaseResponse()

@Serializable
data class PremiumPackListBean(
    @SerialName("packs")
    val premiumPacks: List<PremiumPack>? = listOf(),
)

@Parcelize
@Serializable
data class PremiumPack(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("pack_title")
    val packTitle: String? = null,
    @SerialName("pack_poster_mobile")
    val packImage: String? = null,
    @SerialName("pack_description")
    val packDescriptionForBl: String? = null,
    @SerialName("non_bl_pack_description")
    val packDescriptionForNonBl: String? = null,
    @SerialName("free_pack_details_text")
    val packDescriptionForTrial: String? = null,
    @SerialName("content_id")
    val contentId: List<Int> = listOf(),
    @SerialName("isPurchaseAvailable")
    val isPurchaseAvailable: Int = 0,
    @SerialName("isAvailableFreePeriod")
    val isAvailableFreePeriod: Int = 0,
    @SerialName("isPackPurchased")
    var isPackPurchased: Boolean = false,
    @SerialName("expiryDate")
    var expiryDate: String? = null,
    @SerialName("packDetail")
    var packDetail: String? = null,
) : Parcelable