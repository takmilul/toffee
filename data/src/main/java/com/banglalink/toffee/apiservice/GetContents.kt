package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class GetContents @AssistedInject constructor(
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    private val preference: SessionPreference,
    @Assisted private val requestParams: ChannelRequestParams,
) : BaseApiService<ChannelInfo> {
    
    val gson = Gson()
    
    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO2 {
            toffeeApi.getContents(
                requestParams.type,
                requestParams.categoryId,
                requestParams.subcategoryId,
                offset,
                limit,
                preference.getDBVersionByApiName(ApiNames.GET_CONTENTS_V5),
                ContentRequest(
                    preference.customerId,
                    preference.password,
                )
            )
        }
        
        return response.response.channels?.filter {
            it.isExpired = try {
                Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
            } catch (e: Exception) {
                false
            }
            if (!it.isExpired) {
                it.isFromSportsCategory = (it.isVOD && requestParams.categoryId == 0 && it.categoryId == 16) || requestParams.categoryId == 16
                if (it.isLinear) {
                    localSync.syncData(it, LocalSync.SYNC_FLAG_TV_RECENT, response.isFromCache)
                    localSync.syncData(it, LocalSync.SYNC_FLAG_CDN_CONTENT, response.isFromCache)
                    localSync.syncData(it, LocalSync.SYNC_FLAG_USER_ACTIVITY, response.isFromCache)
                } else {
                    localSync.syncData(it, isFromCache = response.isFromCache)
                }
            }
            !it.isExpired
        } ?: emptyList()
    }
    
    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: ChannelRequestParams): GetContents
    }
}