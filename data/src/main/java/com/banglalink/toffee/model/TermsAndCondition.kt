package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TermsAndCondition(
    @SerialName("terms_and_conditions_white")
    val terms_and_conditions_white: String? = null,
    @SerialName("terms_and_conditions_black")
    val terms_and_conditions_black: String? = null,
    @SerialName("code")
    val code: Int = 0
)