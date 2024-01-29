package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class BaseResponse {
    @SerialName("status")
    var status: Int = 0
    
    var statusCode: Int = 200
    
    @SerialName("product")
    var product: String? = null
    
    @SerialName("version")
    var version: String? = null
    
    @SerialName("apiName")
    var apiName: String? = null
    
    @SerialName("errorCode")
    var errorCode: Int = 0
    
    @SerialName("errorMsg")
    var errorMsg: String? = null
    
    @SerialName("errorMsgTitle")
    var errorMsgTitle: String? = null
    
    @SerialName("displayMsg")
    var displayMsg: String? = null
    
    @SerialName("isFromCache")
    var isFromCache: Boolean = false
}