package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.HistoryContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.ChannelDAO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText
import com.google.gson.Gson

class GetHistory(private val channelDAO: ChannelDAO) {
    private val gson = Gson()

    suspend fun execute(): List<ChannelInfo> = channelDAO.getAll().map {
        gson.fromJson(it.payLoad,ChannelInfo::class.java)
    }.map {
        it.formatted_view_count = getFormattedViewsText(it.view_count)
        it.formattedDuration = discardZeroFromDuration(it.duration)
        it
    }
}