package com.banglalink.toffee.data.network.request

data class UgcMyChannelPlaylistContentRequest (
    val customerId: Int,
    val password: String,
): BaseRequest("getUgcContentByPlaylist")