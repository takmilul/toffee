package com.banglalink.toffee.ui.refer

import kotlinx.serialization.Serializable

@Serializable
data class ReferralForm(
    val referralCode:String? = null,
    val shareableString:String? = null,
    val promotionMessage:String? = null,
    val readMoreMessage:String? = null,
    val fontSize:Int = 0,
    val fontColor:String? = null
)