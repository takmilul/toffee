package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TermsAndCondition(
    @SerialName("terms_and_conditions_white")
    val terms_and_conditions_white: String?,
    @SerialName("terms_and_conditions_black")
    val terms_and_conditions_black: String?,
    @SerialName("code")
    val code: Int = 0
)