package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.response.AudioBookEpisodeResponse
import com.banglalink.toffee.data.network.response.AudioBookSeeMoreResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIOExternal
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class AudioBookEpisodeListService @Inject constructor(
    private val toffeeApi: ExternalApi,
    private val preference: SessionPreference
) {
    suspend fun execute(id : String): AudioBookEpisodeResponse {
        
        return tryIOExternal {
            toffeeApi.audioBookEpisodeList(
                url = "https://api.kabbik.com/v4/toffee/audiobook/$id/${preference.customerId}",
                authorizationToken = "Bearer ${preference.kabbikAccessToken}",
                referrer = "https://toffeelive.com/",
            )
        }
    }
}
