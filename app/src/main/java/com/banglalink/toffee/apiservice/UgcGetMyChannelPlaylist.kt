package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.UgcMyChannelPlaylistRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.UgcChannelPlaylist
import javax.inject.Inject

class UgcGetMyChannelPlaylist@Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi
) {

    suspend fun loadData(isOwner: Int, channelId: Int, offset: Int, limit: Int): List<UgcChannelPlaylist> {
        if(offset > 0) return emptyList()
        val response = tryIO2 {
            toffeeApi.getUgcMyChannelPlaylist(
                isOwner,
                channelId,
                limit,
                offset,
                preference.getDBVersionByApiName("getUgcPlaylistNames"),
                UgcMyChannelPlaylistRequest(preference.customerId, preference.password)
            )
        }

        if (response.response.channelPlaylist != null) {
            return response.response.channelPlaylist
        }
        return emptyList()
    }
}