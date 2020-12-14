package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.SearchContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo

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
        return response.response.channels ?: emptyList()
    }
}