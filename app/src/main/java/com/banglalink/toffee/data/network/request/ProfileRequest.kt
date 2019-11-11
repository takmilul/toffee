package com.banglalink.toffee.data.network.request

data class ProfileRequest(val customerId:Int,val password:String):BaseRequest("getSubscriberProfile")