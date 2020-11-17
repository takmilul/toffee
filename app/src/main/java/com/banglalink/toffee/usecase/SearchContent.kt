package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.SearchContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText

class SearchContent(private val preference: Preference, private val toffeeApi: ToffeeApi) {
    var mOffset: Int = 0
        private set
    private val limit = 30

    suspend fun execute(keyword: String = ""): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.searchContent(
                SearchContentRequest(
                    keyword,
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
                if(!it.created_at.isNullOrBlank()) {
                    it.formattedCreateTime = Utils.getDateDiffInDayOrHourOrMinute(Utils.getDate(it.created_at).time).replace(" ", "")
                }
                it
            }
        }

        return listOf()
    }
}