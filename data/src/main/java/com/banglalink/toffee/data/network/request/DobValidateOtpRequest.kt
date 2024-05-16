package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DobValidateOtpRequest(
    @SerialName("customerId") var customerId: Int? = null,
    @SerialName("password") var password: String? = null,
    @SerialName("otp") var otp: String? = null,
    @SerialName("requestId") val requestId: String? = null,
    @SerialName("paymentMethodId") val paymentMethodId: Int? = null,
    @SerialName("msisdn") val msisdn: String? = null,
) : BaseRequest(ApiNames.DOB_VALIDATE_OTP)