package com.banglalink.toffee.apiservice

import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.model.MyChannelEditBean
import javax.inject.Inject

class MyChannelEditDetailService @Inject constructor(private val toffeeApi: ToffeeApi) {

    suspend fun execute(myChannelEditRequest: MyChannelEditRequest): MyChannelEditBean? {

        val response = tryIO {
            toffeeApi.editMyChannelDetail(myChannelEditRequest)
        }

        return response.response
    }
}