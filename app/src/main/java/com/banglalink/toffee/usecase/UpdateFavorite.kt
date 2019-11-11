package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.FavoriteRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.ui.player.ChannelInfo

class UpdateFavorite(private val preference: Preference, private val toffeeApi: ToffeeApi) {
    private val FAVORITE = 1
    private val REMOVE_FAVORITE = 0

    suspend fun execute(channelInfo: ChannelInfo, favorite: Boolean): ChannelInfo {
        tryIO {
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