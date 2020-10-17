package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.UgcMyChannelDetailRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.UgcMyChannelDetailBean
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

data class MyChannelDetailParam(
    val isOwner: Int = 0,
    val channelId: Int = 0,
)

class GetUgcMyChannelDetail @AssistedInject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi, @Assisted private val requestParams: MyChannelDetailParam) {
    
    suspend fun execute(): UgcMyChannelDetailBean? {
        
        val response = tryIO2 {
            toffeeApi.getUgcMyChannelDetails(
                requestParams.isOwner,
                requestParams.channelId,
                preference.getDBVersionByApiName("getUgcChannelDetails"),
                UgcMyChannelDetailRequest(
                    preference.customerId,
                    preference.password
                )
            )
        }
        
        return response.response
    }
    
    @AssistedInject.Factory
    interface AssistedFactory{
        fun create(requestParams: MyChannelDetailParam): GetUgcMyChannelDetail
    }
}