package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SingleListRepository
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText

class UserActivities(private val preference: Preference, private val toffeeApi: ToffeeApi)
    : SingleListRepository<ChannelInfo> {
    var mOffset: Int = 0
        private set
    private val limit = 10

    override suspend fun execute(): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getContents(
                0, mOffset, "VOD",
                preference.getDBVersionByApiName("getContentsV5"),
                ContentRequest(
                    0,
                    0,
                    "VOD",
                    preference.customerId,
                    preference.password,
                    offset = mOffset,
                    limit = limit
                )
            )
        }


        mOffset += response.response.count
        if (response.response.channels != null) {
            return response.response.channels.map {
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                it
            }
        }
        return listOf()
    }
}