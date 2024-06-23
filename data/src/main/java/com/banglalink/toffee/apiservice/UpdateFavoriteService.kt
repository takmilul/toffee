package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.FavoriteRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.FavoriteBean
import javax.inject.Inject

class UpdateFavoriteService @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {
    
    suspend fun execute(channelInfo: ChannelInfo, favorite: Boolean): FavoriteBean? {
        val response = tryIO {
            toffeeApi.updateFavorite(
                FavoriteRequest(
                    channelInfo.id.toInt(),
                    if (favorite) 1 else 0,
                    preference.customerId,
                    preference.password
                )
            )
        }
        return response.response
    }
}