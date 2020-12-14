package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.HistoryContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo

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
        return response.response.channels ?: emptyList()
    }
}