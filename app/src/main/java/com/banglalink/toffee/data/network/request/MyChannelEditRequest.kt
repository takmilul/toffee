package com.banglalink.toffee.data.network.request

data class MyChannelEditRequest (
    var customerId:Int,
    var password:String,
    var channelId: Int,
    val categoryId: Long,
    val channelName: String,
    val channelDesc: String? = null,
    val oldBannerImageUrl: String? = "NULL",
    val bannerImage: String? = "NULL",
    val oldProfileImageUrl: String? = "NULL",
    val profileImage: String? = "NULL"
): BaseRequest("ugcChannelEdit")