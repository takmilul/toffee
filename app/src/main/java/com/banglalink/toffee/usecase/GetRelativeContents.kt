package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.RelativeContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.ui.player.ChannelInfo

class GetRelativeContents(private val preference: Preference, private val toffeeApi: ToffeeApi){

    suspend fun execute(channelInfo: ChannelInfo, offset: Int, limit: Int = 10): List<ChannelInfo> {
        val response = tryIO {
            toffeeApi.getRelativeContents(
                RelativeContentRequest(
                    channelInfo.id,
                    channelInfo.video_tags,
                    preference.customerId,
                    preference.password,
                    offset,
                    limit
                )
            )
        }

        //filtering out already added current item
        if(response.response.channels!=null){
            return response.response.channels.filter {
                val status = it.program_name.equals(channelInfo.program_name,true)
                !status
            }
        }
        return response.response.channels?:listOf()

    }
}