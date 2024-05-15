package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PackPaymentMethodRequest
import com.banglalink.toffee.data.network.response.PackPaymentMethodBean
import com.banglalink.toffee.data.network.response.PackPaymentMethodResponse
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.NetworkModuleLib
import com.banglalink.toffee.util.EncryptionUtil
import timber.log.Timber
import javax.inject.Inject

class PackPaymentMethodService @Inject constructor (
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference
) {
    
    suspend fun loadData(packId: Int) : PackPaymentMethodBean {
        
        val request =  PackPaymentMethodRequest(
            preference.customerId,
            preference.password
        )
        val isBlNumber = if ( preference.isBanglalinkNumber=="true") 1 else 0
        
        val response = tryIO {
            toffeeApi.getPackPaymentMethods(
                isBlNumber,
                packId,
                preference.getDBVersionByApiName(ApiNames.PACKAGE_WISE_PREMIUM_PACK),
                request
            )
        }

        return response.response
    }
}