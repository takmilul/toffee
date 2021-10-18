package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ReferralCodeBean(
    @SerializedName("referralCode")
    val referralCode:String,
    @SerializedName("sharableText")
    val sharableText:String)