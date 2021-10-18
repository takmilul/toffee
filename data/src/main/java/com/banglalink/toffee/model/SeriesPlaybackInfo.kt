package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SeriesPlaybackInfo(
    val seriesId: Int,
    val serialName: String,
    var seasonNo: Int,
    val totalSeason: Int,
    var channelId: Int = -1,
    var currentItem: ChannelInfo? = null,
    val type: String = "VOD"
): Parcelable {
    fun playlistId(): Long = (seriesId * 100L + seasonNo) * 10L + 2L
}