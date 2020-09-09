package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.HistoryContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelSubscriptionInfo
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText

class GetChannelSubscriptions(private val preference: Preference,
                              private val toffeeApi: ToffeeApi): SingleListRepository<ChannelSubscriptionInfo> {
    var mOffset: Int = 0
        private set
    private val limit = 10

    override suspend fun execute(): List<ChannelSubscriptionInfo> {
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
                ChannelSubscriptionInfo(it.program_name ?: "N/A",
                    it.landscape_ratio_1280_720,
                    it.isLive,
                    it.view_count,
                    it.isLive,
                    if(!it.isLive) 32 else 0,
                    it.isLive,
                    if(it.isLive) "15 Tk" else null,
                    if(it.isLive) 0 else 5,
                    "999K"//getFormattedViewsText(it.view_count)
                )
            }
        }
        return listOf()
    }
}