package com.banglalink.toffee.usecase


import com.banglalink.toffee.data.storage.ChannelDAO
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.discardZeroFromDuration
import com.google.gson.Gson

class GetHistory(private val channelDAO: ChannelDAO,private val getViewCount: GetViewCount) {
    private val gson = Gson()

    suspend fun execute(): List<ChannelInfo> = channelDAO.getAll().map {
        gson.fromJson(it.payLoad,ChannelInfo::class.java)
    }.map {
        it.formatted_view_count = getViewCount(it)
        it.formattedDuration = discardZeroFromDuration(it.duration)
        it
    }
}