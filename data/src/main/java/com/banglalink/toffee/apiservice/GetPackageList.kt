package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PackageListRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.Package
import javax.inject.Inject

class GetPackageList @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute():List<Package>{
        val response = tryIO {
            toffeeApi.getPackageList(PackageListRequest(preference.customerId,preference.password))
        }

        return response.response.packageList?.filter {
            !it.packageType.equals("VOD",true)//Filtering out the VOD
        } ?: emptyList()
    }
}