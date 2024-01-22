package com.banglalink.toffee.ui.refer

import kotlinx.serialization.Serializable

@Serializable
data class ReferralForm(val referralCode:String,val shareableString:String,val promotionMessage:String,val readMoreMessage:String,val fontSize:Int, val fontColor:String)