package com.banglalink.toffee.extension

import com.banglalink.toffee.model.ChannelInfo
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.gms.cast.MediaQueueItem
import com.google.gson.Gson

fun MediaQueueItem.getChannelMetadata(): ChannelInfo? {
    return try {
        Gson().fromJson(customData!!.getString("channel_info"), ChannelInfo::class.java)
    } catch (ex: Exception) {
        null
    }
}

fun MediaItem.getChannelMetadata(player: Player? = null): ChannelInfo? {
    localConfiguration?.tag?.let {
        if(it is ChannelInfo) return it
        else if(it is Int && player is CastPlayer) {
            return player.getItem(it)?.getChannelMetadata()
        }
    }
    return null
}