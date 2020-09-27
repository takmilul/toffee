package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.RelativeContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText

class GetRelativeContents(private val preference: Preference, private val toffeeApi: ToffeeApi,private val getViewCount: GetViewCount){
    var mOffset: Int = 0
        private set
    private val limit = 30
    suspend fun execute(channelInfo: ChannelInfo): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getRelativeContents(
                RelativeContentRequest(
                    channelInfo.id,
                    channelInfo.video_tags,
                    preference.customerId,
                    preference.password,
                    mOffset,
                    limit
                )
            )
        }
        mOffset += response.response.count
        //filtering out already added current item
        if(response.response.channels!=null){
            return response.response.channels.map {
                it.formatted_view_count = getViewCount(it)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                it
            }
        }
        return listOf()

    }
}