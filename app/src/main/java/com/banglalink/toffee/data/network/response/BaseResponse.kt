package com.banglalink.toffee.data.network.response


open class BaseResponse {
    var status: Int = 0
    var product: String? = null
    var version: String? = null
    var apiName: String? = null
    var errorCode: Int = 0
    var errorMsg: String? = null
    var errorMsgTitle: String? = null
    var displayMsg: String? = null
}