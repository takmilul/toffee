package com.banglalink.toffee.ui.bkash.api

import com.banglalink.toffee.model.CustomerInfoLogin
import com.banglalink.toffee.ui.bkash.model.CreatePaymentResponse
import com.banglalink.toffee.ui.bkash.model.GrantTokenResponse
import com.banglalink.toffee.ui.bkash.model.CreatePaymentBodyRequest
import com.banglalink.toffee.ui.bkash.model.GrantTokenBodyRequest
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {
    @POST
    fun postGrantToken(
        @Url url: String?,
        @Header("username") bkashUsername: String?,
        @Header("password") bkashPassword: String?,
        @Body signup: GrantTokenBodyRequest
    ): Call<GrantTokenResponse>

    @POST
    fun postPaymentCreate(
        @Url url: String?,
        @Header("authorization") authorization: String?,
        @Header("x-app-key") xAppKey: String?,
        @Body createPaymentBody: CreatePaymentBodyRequest
    ): Call<CreatePaymentResponse>
}