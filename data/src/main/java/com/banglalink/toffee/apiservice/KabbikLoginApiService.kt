package com.banglalink.toffee.apiservice

import android.util.Log
import com.banglalink.toffee.data.network.request.KabbikLoginApiRequest
import com.banglalink.toffee.data.network.response.KabbikLoginApiResponse
import com.banglalink.toffee.data.network.retrofit.ExternalApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIOExternal
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.util.Utils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

class KabbikLoginApiService @Inject constructor(
    private val externalApi: ExternalApi
) {
    suspend fun execute(): KabbikLoginApiResponse {
        return tryIOExternal {
            Log.d("KabbikApi", "execute login: from network")
            externalApi.kabbikLoginApi(
                url = "https://api.kabbik.com/v1/auth/toffee/login",
                request = KabbikLoginApiRequest(
                    subscriberId = "01783149316",
                    clientId = "toffee",
                    clientSecret = "toffee_dev"
                )
            )
        }
    }
}