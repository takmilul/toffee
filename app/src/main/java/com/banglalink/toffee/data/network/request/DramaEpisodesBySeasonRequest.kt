package com.banglalink.toffee.data.network.request

data class DramaEpisodesBySeasonRequest (
    val customerId:Int,
    val password:String,
): BaseRequest("getUgcDramaSerialBySeason")