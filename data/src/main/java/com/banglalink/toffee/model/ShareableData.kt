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
    val channelOwnerId: Int? = null,
    @SerializedName("playlistId")
    val playlistId: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("seasonNo")
    var seasonNo: Int? = null,
    @SerializedName("serialSummaryId")
    val serialSummaryId: Int? = null,
    @SerializedName("activeSeason")
    val activeSeason: List<Int>? = null,
    @SerializedName("contentType")
    val contentType: String = "VOD",
    @SerializedName("fmRadioShareUrl")
    val fmRadioShareUrl: String? = null,
)