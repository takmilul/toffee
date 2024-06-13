package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShareableData(
    @SerialName("type")
    val type: String? = null,
    @SerialName("categoryId")
    val categoryId: Int? = null,
    @SerialName("channelId")
    val channelId: Int? = null,
    @SerialName("stingrayShareUrl")
    val stingrayShareUrl: String? = null,
    @SerialName("isUserPlaylist")
    val isUserPlaylist: Int? = null,
    @SerialName("isOwner")
    val isOwner: Int? = null,
    @SerialName("channelOwnerUserId")
    val channelOwnerId: Int? = null,
    @SerialName("playlistId")
    val playlistId: Int? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("seasonNo")
    var seasonNo: Int? = null,
    @SerialName("serialSummaryId")
    val serialSummaryId: Int? = null,
    @SerialName("activeSeason")
    val activeSeason: List<Int>? = null,
    @SerialName("contentType")
    val contentType: String = "VOD",
    @SerialName("fmRadioShareUrl")
    val fmRadioShareUrl: String? = null,
)