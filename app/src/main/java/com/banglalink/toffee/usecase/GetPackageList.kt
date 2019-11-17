package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PackageListRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Package

class GetPackageList(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute():List<Package>{
        val response = tryIO {
            toffeeApi.getPackageList(PackageListRequest(preference.customerId,preference.password))
        }
//        preference.setSystemTime(response.response.systemTime)

        return response.response.packageList.filter {
            !it.packageType.equals("VOD",true)//Filtering out the VOD
        }
    }
}