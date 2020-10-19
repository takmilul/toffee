package com.banglalink.toffee.data.network.request

data class AddToPlayListRequest(
    val playlistId: Int,
    val contentId: Int,
    val customerId: Int,
    val password: String
): BaseRequest("ugcAddContentToPlaylist")