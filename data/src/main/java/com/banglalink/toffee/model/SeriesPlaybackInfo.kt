package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class SeriesPlaybackInfo(
    @SerialName("seriesId")
    val seriesId: Int,
    @SerialName("serialName")
    val serialName: String,
    @SerialName("seasonNo")
    var seasonNo: Int,
    @SerialName("totalSeason")
    val totalSeason: Int,
    @SerialName("active_season_list")
    var activeSeasonList: List<Int>? = listOf(1),
    @SerialName("shareUrl")
    var shareUrl: String? = null,
    @SerialName("channelId")
    var channelId: Int = -1,
    @SerialName("currentItem")
    var currentItem: ChannelInfo? = null,
    @SerialName("type")
    val type: String = "VOD"
): Parcelable {
    fun playlistId(): Long = (seriesId * 100L + seasonNo) * 10L + 2L
}