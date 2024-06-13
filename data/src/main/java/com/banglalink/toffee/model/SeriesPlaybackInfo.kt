package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class SeriesPlaybackInfo(
    @SerialName("seriesId")
    val seriesId: Int = 0,
    @SerialName("serialName")
    val serialName: String? = null,
    @SerialName("seasonNo")
    var seasonNo: Int = 0,
    @SerialName("totalSeason")
    val totalSeason: Int = 0,
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