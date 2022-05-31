package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.PlaylistShareableRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

class PlaylistShareableService2 @AssistedInject constructor(
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    private val preference: SessionPreference,
    @Assisted private val requestParams: PlaylistPlaybackInfo,
) : BaseApiService<ChannelInfo> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<ChannelInfo> {
        val isUserPlaylist = if(requestParams.isUserPlaylist) 1 else 0
        val response = tryIO2 {
            toffeeApi.getPlaylistShareable(
                isUserPlaylist,
                requestParams.isOwner,
                requestParams.channelOwnerId,
                requestParams.playlistId,
                limit,
                offset,
                PlaylistShareableRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }
        
        return response.response.channels?.map {
                it.isExpired = try {
                    Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
                } catch (e: Exception) {
                    false
                }
                localSync.syncData(it)
                it
            }?.filter { !it.isExpired } ?: emptyList()
        
    }
    
    @dagger.assisted.AssistedFactory
    interface AssistedFactory {
        fun create(requestParams: PlaylistPlaybackInfo): PlaylistShareableService2
    }
}