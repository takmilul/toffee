package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.FavoriteContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.ui.player.ChannelInfo

class GetFavoriteContents(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(offset: Int): List<ChannelInfo> {
        val response = tryIO {
            toffeeApi.getFavoriteContents(
                FavoriteContentRequest(
                    preference.customerId,
                    preference.password,
                    offset,
                    30
                )
            )
        }

        return response.response.channels?: listOf()
    }
}