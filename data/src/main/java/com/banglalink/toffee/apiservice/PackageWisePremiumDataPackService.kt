package com.banglalink.toffee.apiservice

import android.util.Log
import com.banglalink.toffee.data.network.request.PackageWisePremiumPackRequest
import com.banglalink.toffee.data.network.response.PackageWisePremiumPackResponse

import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ActivePack
import com.banglalink.toffee.usecase.TAG
import javax.inject.Inject

class PackageWisePremiumDataPackService @Inject constructor (
    private val toffeeApi: ToffeeApi,
    private val preference: SessionPreference

) {

    suspend fun loadData(packId: Int) : PackageWisePremiumPackResponse.PackageWisePremiumPackBean {

        val request =  PackageWisePremiumPackRequest(
            preference.customerId,
            preference.password
        )
        var isBlNumber = if ( preference.isBanglalinkNumber=="true") 1 else 0

        val response = tryIO2 {
            toffeeApi.getPackageWisePremiumDataPack(
                isBlNumber,
                packId,
                preference.getDBVersionByApiName(ApiNames.PACKAGE_WISE_PREMIUM_PACK),
                request
            )
        }

        return response.response

    }
}