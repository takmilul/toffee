package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.HistoryContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText

class GetHistory(private val preference: Preference, private val toffeeApi: ToffeeApi) {
    var mOffset: Int = 0
        private set
    private val limit = 10

    suspend fun execute(): List<ChannelInfo> {
        val response = tryIO2 {
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
                if(!it.created_at.isNullOrEmpty()) {
                    it.formattedCreateTime = Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(it.created_at).time).replace(" ", "")
                }
                it.formattedSubscriberCount = getFormattedViewsText(it.subscriberCount.toString())
                it
            }
        }
        return listOf()
    }
}