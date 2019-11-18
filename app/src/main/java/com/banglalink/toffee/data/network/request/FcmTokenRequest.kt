package com.banglalink.toffee.data.network.request

data class FcmTokenRequest(val token:String,val customerId:Int):BaseRequest("setFcmToken")