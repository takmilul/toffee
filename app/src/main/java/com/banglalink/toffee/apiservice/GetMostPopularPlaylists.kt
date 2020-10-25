package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.FavoriteContentRequest
import com.banglalink.toffee.data.network.request.MostPopularContentRequest
import com.banglalink.toffee.data.network.request.MostPopularPlaylistsRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.util.discardZeroFromDuration
import com.banglalink.toffee.util.getFormattedViewsText
import javax.inject.Inject

class GetMostPopularPlaylists @Inject constructor(
    private val preference: Preference,
    private val toffeeApi: ToffeeApi
): BaseApiService<MyChannelPlaylist> {

    override suspend fun loadData(offset: Int, limit: Int): List<MyChannelPlaylist> {
        if(offset > 0)  return emptyList()
        val response = tryIO2 {
            toffeeApi.getMostPopularPlaylists(
                preference.getDBVersionByApiName("getUgcPopularPlaylistNames"),
                MostPopularPlaylistsRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        if (response.response.channelPlaylist != null) {
            return response.response.channelPlaylist
        }
        return emptyList()
    }
}