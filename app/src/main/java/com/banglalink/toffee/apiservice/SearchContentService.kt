package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.SearchContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class SearchContentService @AssistedInject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val keyword: String
) : BaseApiService<ChannelInfo> {

    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.searchContent(
                SearchContentRequest(
                    keyword,
                    preference.customerId,
                    preference.password,
                    offset,
                    limit
                )
            )
        }

        return response.response.channels?.map {
            localSync.syncData(it)
            it
        } ?: emptyList()
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(keyword: String): SearchContentService
    }
}