package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MnpStatusRequest
import com.banglalink.toffee.data.network.response.MnpStatusBean
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.PAYMENT_LOG_FROM_DEVICE
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.usecase.MnpStatusData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MnpStatusService @Inject constructor(
    private val json: Json,
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi
) {
    
    suspend fun execute(): MnpStatusBean? {
        val response = tryIO {
            toffeeApi.getMnpStatus(
                MnpStatusRequest(
                    customerId = preference.customerId,
                    password = preference.password,
                    telcoId = 1
                )
            )
        }

        preference.isPrepaid = response.response?.isPrepaid == true
        preference.isBanglalinkNumber = response.response?.isBlNumber.toString()

        val mnpStatusData = MnpStatusData(
            callingApiName = "mnpStatus",
            rawResponse = json.encodeToString(response)
        )
        PubSubMessageUtil.sendMessage(mnpStatusData, PAYMENT_LOG_FROM_DEVICE)

        return response.response
    }
}