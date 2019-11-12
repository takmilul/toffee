package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.SearchContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.ui.player.ChannelInfo
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText

class SearchContent(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(keyword: String = "", offset: Int):List<ChannelInfo> {
        val response = tryIO {
            toffeeApi.searchContent(
                SearchContentRequest(
                    keyword,
                    preference.customerId,
                    preference.password,
                    offset,
                    30
                )
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
}