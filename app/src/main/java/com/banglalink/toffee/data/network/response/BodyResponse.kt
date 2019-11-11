package com.banglalink.toffee.data.network.response

open class BodyResponse {
    var code: Int = 0
    var notification: Boolean = false
    var notificationType: Int = 0
    var ads: Boolean = false
    var adsType: Int = 0
    var message: String? = null
    var messageType: String? = null
}