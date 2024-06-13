package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PremiumPackSubHistoryResponse(
    @SerialName("response")
    var response: SubHistoryResponseBean? = SubHistoryResponseBean(),
) : BaseResponse()

@Serializable
data class SubHistoryResponseBean(
    @SerialName("subs_history_details")
    var subsHistoryDetails: List<SubsHistoryDetail> = listOf(),
    @SerialName("history_showing_text")
    var historyShowingText: String? = null,
)

@Serializable
data class SubsHistoryDetail(
    @SerialName("pack_name")
    var packName: String? = null,
    @SerialName("payment_method")
    var paymentMethod: String? = null,
    @SerialName("payment_method_id")
    var paymentMethodId: Int? = null,
    @SerialName("payment_price")
    var paymentPrice: String? = null,
    @SerialName("plan")
    var plan: String? = null,
    @SerialName("auto_renewal")
    var autoRenewal: String? = null,
    @SerialName("expiry_time")
    var expiryTime: String? = null,
    @SerialName("subscription_status")
    var subscriptionStatus: String? = null,
    @SerialName("pack_start_date_for_order")
    var packStartDateForOrder: String? = null,
    @SerialName("tooltip_message")
    var tooltipMessage: String? = null,
)