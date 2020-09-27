package com.banglalink.toffee.data.network.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface DbApi{

    @Streaming
    @GET
    fun downloadFile(@Url fileUrl: String): Call<ResponseBody>
}