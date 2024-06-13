package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.response.AudioBookSeeMoreResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIOExternal
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class AudioBookSeeMoreService @Inject constructor(
    private val toffeeApi: ExternalApi,
) {
    suspend fun execute(myTitle : String, token: String): AudioBookSeeMoreResponse {
        
        return tryIOExternal {
            toffeeApi.audioBookSeeMoreList(
                url = "https://api.kabbik.com/v1/audiobooks/home/seemore",
                authorizationToken = "Bearer $token",
                referrer = "https://toffeelive.com/",
                name = myTitle
            )
        }
    }
}
