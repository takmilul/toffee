package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.PackageChannelListRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import javax.inject.Inject

class GetPackageChannels @Inject constructor(private val preference: SessionPreference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(packageId:Int):List<ChannelInfo>{
        val response = tryIO2 {
            toffeeApi.getPackageChannelList(PackageChannelListRequest(packageId,preference.customerId,preference.password))
        }
        return response.response.packageDetails.programs.filter {
            try {
                Utils.getDate(it.contentExpiryTime).after(preference.getSystemTime())
            } catch (e: Exception) {
                true
            }
        }

    }
}