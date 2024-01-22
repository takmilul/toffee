package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class BodyResponse {
    @SerialName("code")
    var code: Int = 0
    
    @SerialName("notification")
    var notification: Boolean = false
    
    @SerialName("notificationType")
    var notificationType: Int = 0
    
    @SerialName("ads")
    var ads: Boolean = false
    
    @SerialName("adsType")
    var adsType: Int = 0
    
    @SerialName("message")
    var message: String? = null
    
    @SerialName("messageType")
    var messageType: String? = null
    
    @SerialName("lat")
    var lat: String? = ""
    
    @SerialName("long")
    var lon: String? = ""
}