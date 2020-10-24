package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.MyChannelPlaylistVideosRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class MyChannelPlaylistContentParam(val channelId: Int, val isOwner: Int, val playlistId: Int)

class MyChannelPlaylistContentService @AssistedInject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi, @Assisted private val requestParams: MyChannelPlaylistContentParam):
    BaseApiService<ChannelInfo> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getMyChannelPlaylistVideos(
                requestParams.channelId,
                requestParams.isOwner, 
                requestParams.playlistId,
                limit, offset,
                preference.getDBVersionByApiName("getUgcChannelAllContent"),
                MyChannelPlaylistVideosRequest(preference.customerId, preference.password)
            )
        }

        if (response.response.channels != null) {
            return response.response.channels.map {
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                it
            }
        }
        return listOf()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(requestParams: MyChannelPlaylistContentParam): MyChannelPlaylistContentService
    }
}