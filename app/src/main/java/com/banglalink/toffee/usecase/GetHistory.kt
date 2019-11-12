package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.HistoryContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.ui.player.ChannelInfo
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText

class GetHistory(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(offset: Int): List<ChannelInfo> {
        val response = tryIO {
            toffeeApi.getHistoryContents(
                HistoryContentRequest(
                    preference.customerId,
                    preference.password,
                    offset,
                    30
                )
            )
        }


        preference.balance = response.response.balance

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