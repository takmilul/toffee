package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MostPopularPlaylistsRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.Utils.getDate
import javax.inject.Inject

class GetMostPopularPlaylists @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
): BaseApiService<MyChannelPlaylist> {

    override suspend fun loadData(offset: Int, limit: Int): List<MyChannelPlaylist> {
        
        val response = tryIO2 {
            toffeeApi.getMostPopularPlaylists(
                preference.getDBVersionByApiName(ApiNames.GET_UGC_POPULAR_PLAYLIST_NAMES),
                MostPopularPlaylistsRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }

        if (response.response.channelPlaylist != null) {
            response.response.channelPlaylist.map { 
                it.formattedCreateTime = Utils.getDateDiffInDayOrHourOrMinute(getDate(it.created_at).time).replace(" ", "")
            }
            return response.response.channelPlaylist
        }
        return emptyList()
    }
}