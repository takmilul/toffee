package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.UgcEditMyChannelRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.UgcEditMyChannelBean
import javax.inject.Inject

class EditMyChannel @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(ugcEditMyChannelRequest: UgcEditMyChannelRequest): UgcEditMyChannelBean {

        val response = tryIO2 {
            toffeeApi.ugcEditMyChannel(
                ugcEditMyChannelRequest.apply { 
                    customerId = preference.customerId
                    password = preference.password
                }
            )
        }

        return response.response
    }
}