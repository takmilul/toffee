package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.FavoriteRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import javax.inject.Inject

class UpdateFavorite @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {
    private val FAVORITE = 1
    private val REMOVE_FAVORITE = 0

    suspend fun execute(channelInfo: ChannelInfo, favorite: Boolean): ChannelInfo {
        tryIO2 {
            toffeeApi.updateFavorite(
                FavoriteRequest(
                    channelInfo.id.toInt(),
                    if (favorite) FAVORITE else REMOVE_FAVORITE,
                    preference.customerId,
                    preference.password
                )
            )
        }

        channelInfo.favorite = if(favorite) "1" else "0"
        return channelInfo
    }
}