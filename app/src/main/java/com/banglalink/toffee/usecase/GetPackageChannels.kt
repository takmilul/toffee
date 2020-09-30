package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PackageChannelListRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.ChannelInfo

class GetPackageChannels(private val preference: Preference,private val toffeeApi: ToffeeApi) {

    suspend fun execute(packageId:Int):List<ChannelInfo>{
        val response = tryIO2 {
            toffeeApi.getPackageChannelList(PackageChannelListRequest(packageId,preference.customerId,preference.password))
        }
        return response.response.packageDetails.programs

    }
}