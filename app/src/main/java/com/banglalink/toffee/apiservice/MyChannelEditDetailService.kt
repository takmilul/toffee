package com.banglalink.toffee.apiservice

import android.util.Log
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.MyChannelEditBean
import javax.inject.Inject

class MyChannelEditDetailService @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) {

    suspend fun execute(myChannelEditRequest: MyChannelEditRequest): MyChannelEditBean {

        val request = myChannelEditRequest.apply {
            customerId = preference.customerId
            password = preference.password
        }
        println("ugcChannelEdit")
        println("Request: $request")
        val response = tryIO2 {
            toffeeApi.editMyChannelDetail(
                myChannelEditRequest.apply { 
                    customerId = preference.customerId
                    password = preference.password
                    channelId = preference.customerId
                }
            )
        }
        Log.i("api TAG", myChannelEditRequest.toString())

        return response.response
    }
}