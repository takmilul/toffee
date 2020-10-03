package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

open class BodyResponse {
    var code: Int = 0
    var notification: Boolean = false
    var notificationType: Int = 0
    var ads: Boolean = false
    var adsType: Int = 0
    var message: String? = null
    var messageType: String? = null
    @SerializedName("lat")
    var lat:String? = ""
    @SerializedName("long")
    var long:String? = ""
}