package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

open class BodyResponse {
    @SerializedName("code")
    var code: Int = 0
    
    @SerializedName("notification")
    var notification: Boolean = false
    
    @SerializedName("notificationType")
    var notificationType: Int = 0
    
    @SerializedName("ads")
    var ads: Boolean = false
    
    @SerializedName("adsType")
    var adsType: Int = 0
    
    @SerializedName("message")
    var message: String? = null
    
    @SerializedName("messageType")
    var messageType: String? = null
    
    @SerializedName("lat")
    var lat: String? = ""
    
    @SerializedName("long")
    var lon: String? = ""
}