package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.TVChannelItem
import com.banglalink.toffee.data.network.request.StingrayContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GetStingrayContentService @Inject constructor(
    private val json: Json,
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    private val preference: SessionPreference,
    private val tvChannelRepo: TVChannelRepository
): BaseApiService<ChannelInfo> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val response = tryIO {
            toffeeApi.getStingrayContents(
                "stingray",
                0,
                0,
                offset,
                limit,
                preference.getDBVersionByApiName(ApiNames.GET_STINGRAY_CONTENTS),
                StingrayContentRequest(
                    preference.customerId,
                    preference.password,
                    1,
                    "stingray"
                )
            )
        }
        val dbList = mutableListOf<TVChannelItem>()
        val upTime = System.currentTimeMillis()
        response.response.channels?.filter {
            it.isExpired = try {
                Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
            } catch (e: Exception) {
                false
            }
            localSync.syncData(it, isFromCache = response.isFromCache)
            
            if (!it.isExpired) {
                dbList.add(
                    TVChannelItem(
                        it.id.toLong(),
                        it.type ?: "Stingray",
                        1,
                        "Music Playlist",
                        json.encodeToString(it),
                        it.view_count?.toLong() ?: 0L,
                        it.isStingray
                    ).apply {
                        updateTime = upTime
                    }
                )
            }
            !it.isExpired
        }
        tvChannelRepo.insertNewItems(*dbList.toTypedArray())
        return response.response.channels ?: emptyList()
    }
}