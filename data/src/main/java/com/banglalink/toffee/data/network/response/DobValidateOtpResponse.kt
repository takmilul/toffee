package com.banglalink.toffee.data.network.response

import com.banglalink.toffee.model.ActivePack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DobValidateOtpBaseResponse(
    @SerialName("response") var response: DobValidateOtpResponseBean? = DobValidateOtpResponseBean(),
) : BaseResponse()

@Serializable
data class DobValidateOtpResponseBean(
    @SerialName("status") var status: Boolean? = null,
    @SerialName("response_from_where") var responseFromWhere: Int? = null,
    @SerialName("payment_method_id") var paymentMethodId: Int? = null,
    @SerialName("message") var message: String? = null,
    @SerialName("login_related_subs_history") var loginRelatedSubsHistory: List<ActivePack>? = null
)