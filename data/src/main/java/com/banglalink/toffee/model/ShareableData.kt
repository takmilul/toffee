package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class ShareableData(
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("categoryId")
    val categoryId: Int? = null,
    @SerializedName("channelId")
    val channelId: Int? = null,
    @SerializedName("stingrayShareUrl")
    val stingrayShareUrl: String? = null,
    @SerializedName("isUserPlaylist")
    val isUserPlaylist: Int? = null,
    @SerializedName("isOwner")
    val isOwner: Int? = null,
    @SerializedName("channelOwnerUserId")
    val channelOwnerUserId: Int? = null,
    @SerializedName("playlistId")
    val playlistId: Int? = null
)