package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.request.AllChannelRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.repository.UserActivitiesRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Utils
import com.google.gson.Gson
import javax.inject.Inject

class GetChannelWithCategory @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    private val preference: SessionPreference,
    private val tvChannelRepo: TVChannelRepository,
    private val userActivityRepo: UserActivitiesRepository,
) {
    val gson = Gson()
    
    suspend fun loadData(subcategoryId: Int) {
        val response = tryIO {
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
                it.isExpired = try {
                    Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
                } catch (e: Exception) {
                    false
                }
                localSync.syncData(it, isFromCache = response.isFromCache)
                
                if (!it.isExpired) {
                    dbList.add(TVChannelItem(
                        it.id.toLong(),
                        it.type ?: "LIVE",
                        index + 1,
                        channelCategory.categoryName,
                        gson.toJson(it),
                        it.view_count?.toLong() ?: 0L,
                        it.isStingray,
                    ).apply {
                        updateTime = upTime
                    })
                }
                !it.isExpired
            }
        }
        tvChannelRepo.getNonStingrayRecentItems()?.forEach { tvChannelItem ->
            if(dbList.none { it.channelId == tvChannelItem.channelId }) {
                tvChannelRepo.deleteItems(tvChannelItem)
            }
        }
        userActivityRepo.getUserActivityListByType("LIVE")?.forEach { userActivity ->
            if (dbList.none { it.channelId == userActivity.channelId }) {
                userActivityRepo.delete(userActivity)
            }
        }
        tvChannelRepo.insertNewItems(*dbList.toTypedArray())
    }
}