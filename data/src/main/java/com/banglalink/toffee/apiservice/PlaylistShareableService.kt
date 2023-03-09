package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PlaylistShareableRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelPlaylistVideosBean
import com.banglalink.toffee.model.ShareableData
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class PlaylistShareableService @AssistedInject constructor(
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference,
    @Assisted private val requestParams: ShareableData,
) {
    
    suspend fun loadData(offset: Int, limit: Int): MyChannelPlaylistVideosBean {
        val response = tryIO {
            toffeeApi.getPlaylistShareable(
                requestParams.isUserPlaylist ?: 0,
                requestParams.isOwner ?: 0,
                requestParams.channelOwnerId ?: 0,
                requestParams.playlistId ?: 0,
                limit,
                offset,
                PlaylistShareableRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }
        
        return response.response.apply {
            channels?.map {
                it.isExpired = try {
                    Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
                } catch (e: Exception) {
                    false
                }
                it
            }?.filter { !it.isExpired }
        }
    }
    
    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: ShareableData): PlaylistShareableService
    }
}