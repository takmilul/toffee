package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class TermsAndCondition(
    @SerializedName("terms_and_conditions_white")
    val terms_and_conditions_white: String?,
    @SerializedName("terms_and_conditions_black")
    val terms_and_conditions_black: String?,
    @SerializedName("code")
    val code: Int = 0
)