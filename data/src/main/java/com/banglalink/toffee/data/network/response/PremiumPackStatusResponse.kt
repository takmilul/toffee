package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ActivePack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PremiumPackStatusResponse(
    @SerialName("apiLanguage")
    val apiLanguage: String,
    @SerialName("debugCode")
    val debugCode: Int,
    @SerialName("debugMsg")
    val debugMsg: String,
    @SerialName("response")
    val response: PremiumPackStatusBean,
) : BaseResponse()

@Serializable
data class PremiumPackStatusBean(
    @SerialName("status")
    var status: Int?,
    @SerialName("login_related_subs_history")
    val loginRelatedSubsHistory: List<ActivePack>?
)
