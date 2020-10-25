package com.banglalink.toffee.data.network.request

data class MyChannelRatingRequest (
    val channelId: Int,
    val rating: Float,
    val customerId:Int,
    val password:String
): BaseRequest("ugcRatingOnChannel")
