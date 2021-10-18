package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MqttRequest
import com.banglalink.toffee.data.network.response.MqttBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.storage.SessionPreference
import javax.inject.Inject

class MqttCredentialService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference
) {
    
    suspend fun execute(): MqttBean? {
        val response = toffeeApi.getMqttCredential(
            MqttRequest(
                preference.customerId,
                preference.password
            )
        )
        return response.response
    }
}