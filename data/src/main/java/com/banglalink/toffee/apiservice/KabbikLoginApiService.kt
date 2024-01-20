package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.KabbikLoginApiRequest
import com.banglalink.toffee.data.network.response.KabbikLoginApiResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIOExternal
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class KabbikLoginApiService @Inject constructor(
    private val externalApi: ExternalApi,
    private val preference: SessionPreference
) {
    suspend fun execute(): KabbikLoginApiResponse {
        return tryIOExternal {
            externalApi.kabbikLoginApi(
                url = "https://api.kabbik.com/v1/auth/toffee/login",
                request = KabbikLoginApiRequest(
                    subscriberId = preference.customerId.toString(),
                    clientId = "toffee-client-2024",
                    clientSecret = "tONbKnVJsmc4g6cS"
                )
            )
        }
    }
}