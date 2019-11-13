package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.ViewingContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.ui.player.ChannelInfo

class SendViewContentEvent(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(channel: ChannelInfo) :Boolean{
         tryIO {
            toffeeApi.sendViewingContent(
                ViewingContentRequest(
                    channel.type,
                    channel.id.toInt(),
                    preference.customerId,
                    preference.password,
                    preference.latitude,
                    preference.longitude
                )
            )
        }
        return true
    }
}