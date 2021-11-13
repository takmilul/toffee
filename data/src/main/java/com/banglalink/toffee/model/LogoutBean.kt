package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class LogoutBean(
    @SerializedName("customerId")
    val customerId:Int,
    @SerializedName("password")
    val password:String,
    @SerializedName("verified_status")
    val verifyStatus: Boolean
)