package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

open class ExternalBaseResponse {
    @SerializedName("status")
    var status: Int = 200
    
    @SerializedName("version")
    var version: String? = null
    
    @SerializedName("errorCode")
    var errorCode: Int = 0
    
    @SerializedName("errorMsg")
    var errorMsg: String? = null
    
    @SerializedName("isFromCache")
    var isFromCache: Boolean = false
}