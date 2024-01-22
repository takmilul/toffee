package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class ExternalBaseResponse {
    @SerialName("statusCode")
    var statusCode: Int = 200
    
    @SerialName("version")
    var version: String? = null
    
    @SerialName("errorCode")
    var errorCode: Int = 0
    
    @SerialName("errorMsg")
    var errorMsg: String? = null
    
    @SerialName("isFromCache")
    var isFromCache: Boolean = false
}