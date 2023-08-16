package com.banglalink.toffee.extension

import androidx.media3.cast.CastPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.banglalink.toffee.model.ChannelInfo
import com.google.android.gms.cast.MediaQueueItem
import com.google.gson.Gson

fun MediaQueueItem.getChannelMetadata(): ChannelInfo? {
    return try {
        Gson().fromJson(customData!!.getString("channel_info"), ChannelInfo::class.java)
    } catch (ex: Exception) {
        null
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun MediaItem.getChannelMetadata(player: Player? = null): ChannelInfo? {
    localConfiguration?.tag?.let {
        if(it is ChannelInfo) return it
        else if(it is Int && player != null && player is CastPlayer) {
            return player.getItem(it)?.getChannelMetadata()
        }
    }
    return null
}