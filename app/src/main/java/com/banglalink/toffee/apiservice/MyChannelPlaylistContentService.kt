package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.storage.Preference
import javax.inject.Inject

class MyChannelPlaylistContentService @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) {
    suspend fun execute(channelId: Int, isOwner: Int, playlistId: Int){
        
    }
}