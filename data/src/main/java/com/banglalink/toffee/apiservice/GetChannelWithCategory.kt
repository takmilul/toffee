package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.LocalSync.Companion.SYNC_FLAG_TV_RECENT
import com.banglalink.toffee.data.database.LocalSync.Companion.SYNC_FLAG_USER_ACTIVITY
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.request.AllChannelRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Utils
import com.google.gson.Gson
import javax.inject.Inject

class GetChannelWithCategory @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    private val preference: SessionPreference,
    private val tvChannelRepo: TVChannelRepository
) {
    val gson = Gson()
    
    suspend operator fun invoke(subcategoryId: Int) {
        val response = tryIO2 {
            toffeeApi.getChannels(
                preference.getDBVersionByApiName(ApiNames.GET_ALL_TV_CHANNELS),
                AllChannelRequest(
                    subcategoryId,
                    preference.customerId,
                    preference.password
                )
            )
        }
        val dbList = mutableListOf<TVChannelItem>()
        val upTime = System.currentTimeMillis()
        response.response.channelCategoryList.forEachIndexed { index, channelCategory ->
            channelCategory.channels?.filter {
                try {
                    Utils.getDate(it.contentExpiryTime).after(preference.getSystemTime())
                } catch (e: Exception) {
                    true
                }
            }?.forEach {
                localSync.syncData(it, SYNC_FLAG_TV_RECENT)
                localSync.syncData(it, SYNC_FLAG_USER_ACTIVITY)
                dbList.add(TVChannelItem(
                    it.id.toLong(),
                    it.type ?: "LIVE",
                    index + 1,
                    channelCategory.categoryName,
                    gson.toJson(it),
                    it.view_count?.toLong() ?: 0L,
                    it.isStingray
                ).apply {
                    updateTime = upTime
                })
            }
        }
        tvChannelRepo.insertNewItems(*dbList.toTypedArray())
    }
}