package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SeriesPlaybackInfo(
    val seriesId: Int,
    val serialName: String,
    val seasonNo: Int,
    val totalSeason: Int,
    var channelId: Int = -1,
    var currentItem: ChannelInfo? = null,
    val type: String = "VOD"
): Parcelable