package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.response.KabbikHomeApiResponse
import com.banglalink.toffee.data.network.response.KabbikTopBannerApiResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIOExternal
import javax.inject.Inject

class KabbikTopBannerApiService @Inject constructor(
    private val externalApi: ExternalApi
) {
    suspend fun execute(token : String): KabbikTopBannerApiResponse {
        return tryIOExternal {
            externalApi.kabbikTopBanner(
                url = "https://api.kabbik.com/v4/toffee/home/top-banner",
                token = "Bearer $token"
            )
        }
    }
}