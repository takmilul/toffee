package com.banglalink.toffee.apiservice

import android.util.Log
import com.banglalink.toffee.data.network.request.KabbikLoginApiRequest
import com.banglalink.toffee.data.network.response.KabbikHomeApiResponse
import com.banglalink.toffee.data.network.response.KabbikLoginApiResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIOExternal
import javax.inject.Inject

class KabbikHomeApiService @Inject constructor(
    private val externalApi: ExternalApi
) {
    suspend fun execute(token : String): KabbikHomeApiResponse {
        return tryIOExternal {
            externalApi.kabbikHomeApi(
                url = "https://api.kabbik.com/v4/toffee/home/free",
                token = "Bearer $token"
            )
        }
    }
}