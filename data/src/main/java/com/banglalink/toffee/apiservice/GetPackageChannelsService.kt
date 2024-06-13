package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.network.request.PackageChannelListRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.Utils
import javax.inject.Inject

class GetPackageChannelsService @Inject constructor(
    private val toffeeApi: ToffeeApi,
    private val localSync: LocalSync,
    private val preference: SessionPreference,
) {
    
    suspend fun execute(packageId:Int):List<ChannelInfo>{
        val response = tryIO {
            toffeeApi.getPackageChannelList(PackageChannelListRequest(packageId,preference.customerId,preference.password))
        }
        return response.response?.packageDetails?.programs?.filter {
            it.isExpired = try {
                Utils.getDate(it.contentExpiryTime).before(preference.getSystemTime())
            } catch (e: Exception) {
                false
            }
            localSync.syncData(it, isFromCache = response.isFromCache)
            !it.isExpired
        } ?: emptyList()
    }
}