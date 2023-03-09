package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.SubscribePackageRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.Package
import javax.inject.Inject

class SubscribePackage @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(mPackage: Package, autoRenew: Boolean = false): String {
        val response = tryIO {
            toffeeApi.subscribePackage(
                SubscribePackageRequest(
                    mPackage.packageId,
                    preference.customerId,
                    preference.password,
                    if (autoRenew) "true" else "false"
                )
            )
        }
        return response.response.message !!
    }
}