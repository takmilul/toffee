package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName


open class BaseResponse {
    @SerializedName("status")
    var status: Int = 0
    
    @SerializedName("product")
    var product: String? = null
    
    @SerializedName("version")
    var version: String? = null
    
    @SerializedName("apiName")
    var apiName: String? = null
    
    @SerializedName("errorCode")
    var errorCode: Int = 0
    
    @SerializedName("errorMsg")
    var errorMsg: String? = null
    
    @SerializedName("errorMsgTitle")
    var errorMsgTitle: String? = null
    
    @SerializedName("displayMsg")
    var displayMsg: String? = null
}