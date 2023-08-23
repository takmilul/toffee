package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class PremiumPackSubHistoryResponse(
    @SerializedName("response")
    var response: SubHistoryResponseBean? = SubHistoryResponseBean(),
) : BaseResponse()

data class SubHistoryResponseBean(
    @SerializedName("subs_history_details")
    var subsHistoryDetails: List<SubsHistoryDetail> = listOf(),
    @SerializedName("history_showing_text")
    var historyShowingText: String? = null
)

data class SubsHistoryDetail(
    @SerializedName("pack_name")
    var packName: String? = null,
    @SerializedName("payment_method")
    var paymentMethod: String? = null,
    @SerializedName("payment_method_id")
    var paymentMethodId: Int? = null,
    @SerializedName("payment_price")
    var paymentPrice: String? = null,
    @SerializedName("plan")
    var plan: String? = null,
    @SerializedName("auto_renewal")
    var autoRenewal: String? = null,
    @SerializedName("expiry_time")
    var expiryTime: String? = null,
    @SerializedName("subscription_status")
    var subscriptionStatus: String? = null,
    @SerializedName("pack_start_date_for_order")
    var packStartDateForOrder: String? = null,
    @SerializedName("tooltip_message")
    var tooltipMessage: String? = null,
)