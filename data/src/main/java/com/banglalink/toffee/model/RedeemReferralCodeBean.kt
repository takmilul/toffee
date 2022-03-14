package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

class RedeemReferralCodeBean(
    @SerializedName("referralStatus")
    val referralStatus: String,
    @SerializedName("referralStatusMessage")
    val referralStatusMessage: String,
    @SerializedName("isRedeemSuccess")
    val isRedeemSuccess:Boolean?=false,
    @SerializedName("messageTitle")
    val title:String?=null,
    @SerializedName("messageBody")
    val message:String?=null,
    @SerializedName("isBulletPointMessage")
    val isBullterPointMessage:Boolean? = false,
    @SerializedName("bulletMessage")
    val bulletMessage:List<String>?,
    @SerializedName("messageType")
    val messageType:String?
)