package com.banglalink.toffee.data.network.request

data class UgcEditMyChannelRequest (
    var customerId:Int,
    var password:String,
    val channelId: Long,
    val categoryId: Int,
    val channelName: String,
    val channelDesc: String? = null,
    val oldBannerImageUrl: String? = "NULL",
    val bannerImage: String? = "NULL",
    val oldProfileImageUrl: String? = "NULL",
    val profileImage: String? = "NULL"
): BaseRequest("ugcChannelEdit")