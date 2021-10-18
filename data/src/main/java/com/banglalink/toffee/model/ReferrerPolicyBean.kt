package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ReferrerPolicyBean(
    @SerializedName("isPromotionMessageEnabled")
    val isPromotionMessageEnabled:Boolean,
    @SerializedName("promotionMessage")
    val promotionMessage:String?,
    @SerializedName("messageReadMoreEnabled")
    val messageReadMoreEnabled:Boolean,
    @SerializedName("readMoreDetails")
    val readMoreDetails:String?,
    @SerializedName("fontSize")
    val fontSize:Int,
    @SerializedName("fontColor")
    val fontColor:String
)