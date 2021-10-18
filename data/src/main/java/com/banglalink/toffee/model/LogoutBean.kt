package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class LogoutBean(
    val customerId:Int,
    val password:String,
    @SerializedName("verified_status")
    val verifyStatus: Boolean
)