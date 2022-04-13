package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SeriesPlaybackInfo(
    @SerializedName("seriesId")
    val seriesId: Int,
    @SerializedName("serialName")
    val serialName: String,
    @SerializedName("seasonNo")
    var seasonNo: Int,
    @SerializedName("totalSeason")
    val totalSeason: Int,
    @SerializedName("active_season_list")
    var activeSeasonList: List<Int>? = listOf(1),
    @SerializedName("shareUrl")
    var shareUrl: String? = null,
    @SerializedName("channelId")
    var channelId: Int = -1,
    @SerializedName("currentItem")
    var currentItem: ChannelInfo? = null,
    @SerializedName("type")
    val type: String = "VOD"
): Parcelable {
    fun playlistId(): Long = (seriesId * 100L + seasonNo) * 10L + 2L
}