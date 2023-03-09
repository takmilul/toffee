package com.banglalink.toffee.data.network.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class PremiumPackListResponse(
    @SerializedName("response")
    val response: PremiumPackListBean? = PremiumPackListBean()
) : BaseResponse()

data class PremiumPackListBean(
    @SerializedName("packs")
    val premiumPacks: List<PremiumPack>? = listOf()
)

@Parcelize
data class PremiumPack(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("pack_title")
    val packTitle: String? = null,
    @SerializedName("pack_poster_mobile")
    val packImage: String? = null,
    @SerializedName("pack_description")
    val packSubtitle: String? = null,
    @SerializedName("free_pack_details_text")
    val freePackDetailsText: String? = null,
    @SerializedName("content_id")
    val contentId: List<Int> = listOf(),
    @SerializedName("isPurchaseAvailable")
    val isPurchaseAvailable: Int = 0,
    @SerializedName("isAvailableFreePeriod")
    val isAvailableFreePeriod: Int = 0,
    @SerializedName("isPackPurchased")
    var isPackPurchased: Boolean = false,
    @SerializedName("expiryDate")
    var expiryDate: String? = null,
    @SerializedName("packDetail")
    var packDetail: String? = null
): Parcelable