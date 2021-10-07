package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.SearchContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class SearchContentService @AssistedInject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    @Assisted private val keyword: String,
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

        return response.response.channels?.filter {
            try {
                Utils.getDate(it.contentExpiryTime).after(preference.getSystemTime())
            } catch (e: Exception) {
                true
            }
        }?.map {
            localSync.syncData(it)
            it
        } ?: emptyList()
    }

    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(keyword: String): SearchContentService
    }
}