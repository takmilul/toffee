package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.request.ChannelRequestParams
import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.TVChannelRepository
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
    private val tvChannelRepo: TVChannelRepository,
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
    
        val dbList = mutableListOf<TVChannelItem>()
        val upTime = System.currentTimeMillis()
        val idList = mutableListOf<Long>()
        var index = 0
        response.response.channels?.map {
            it.isExpired = try {
                Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
            } catch (e: Exception) {
                false
            }
            it.category = requestParams.category
            it.categoryId = requestParams.categoryId
            it.subCategoryId = requestParams.subcategoryId
            it.subCategory = requestParams.subcategory
            if (!it.isExpired && it.isLive && requestParams.categoryId == 16) {
                it.isFromSportsCategory = true
                idList.add(it.id.toLong())
                dbList.add(
                    TVChannelItem(
                    it.id.toLong(),
                    it.type ?: "LIVE",
                    0,
                    "",
                    gson.toJson(it),
                    it.view_count?.toLong() ?: 0L,
                    it.isStingray,
                        true,
                        ++index
                ).apply {
                    updateTime = upTime
                })
            }
            localSync.syncData(it)
            it
        }
        
        if (idList.isNotEmpty()) {
            val count = tvChannelRepo.getLinearChannelsCount()
            if (count > 0) {
                tvChannelRepo.updateIsSportsChannel(idList)
            } else {
                tvChannelRepo.insertNewItems(*dbList.toTypedArray())
            }
        }
        return response.response.channels ?: emptyList()
    }
    
    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: ChannelRequestParams): GetContents
    }
}