package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.HistoryContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelVideo
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.util.Utils.discardZeroFromDuration
import com.banglalink.toffee.util.Utils.getFormattedViewsText

class GetChannelPlaylistVideos(private val preference: Preference, private val toffeeApi: ToffeeApi): SingleListRepository<ChannelVideo> {
    
    var mOffset: Int = 0
        private set
    private val limit = 10
    
    override suspend fun execute(): List<ChannelVideo> {
        val response = tryIO {
            toffeeApi.getHistoryContents(
                HistoryContentRequest(
                    preference.customerId,
                    preference.password,
                    mOffset,
                    limit
                )
            )
        }
        
        mOffset += response.response.count
        if (response.response.channels != null) {
            return response.response.channels.map {
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                it
            }.map {
                ChannelVideo(it.id, it.poster_url_mobile, it.formattedDuration, it.program_name, it.view_count, it.formatted_view_count, it.formatted_view_count)
            }
        }
        return listOf()
    }
}