package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.SearchContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Log
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
        if (keyword.isBlank()) return emptyList()
        
        val response = tryIO {
            toffeeApi.searchContent(
                offset,
                limit,
                preference.getDBVersionByApiName(ApiNames.GET_SEARCH_CONTENTS),
                keyword,
                SearchContentRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }
        
        return response.response?.channels?.filter {
            it.isExpired = try {
                Log.i("SEAR_", "map: ${it.program_name}, isExpired: ${it.isExpired}")
                Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
            } catch (e: Exception) {
                false
            }
            it.isFromSportsCategory = (it.isVOD && it.categoryId == 16)
            it.totalCount = response.response.totalCount
            localSync.syncData(it, isFromCache = response.isFromCache)
            !it.isExpired
        } ?: emptyList()
    }
    
    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(keyword: String): SearchContentService
    }
}