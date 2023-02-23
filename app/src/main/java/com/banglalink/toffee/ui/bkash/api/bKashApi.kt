package com.banglalink.toffee.ui.bkash.api

import com.banglalink.toffee.ui.bkash.model.CreatePaymentResponse
import com.banglalink.toffee.ui.bkash.model.GrantTokenResponse
import com.banglalink.toffee.ui.bkash.model.CreatePaymentBodyRequest
import com.banglalink.toffee.ui.bkash.model.GrantTokenBodyRequest
import retrofit2.Call
import retrofit2.http.*


interface ApiInterface {
    @POST("/v1.2.0-beta/tokenized/checkout/token/grant")
    fun postGrantToken(
        @Header("username") username: String?,
        @Header("password") password: String?,
        @Body signup: GrantTokenBodyRequest
    ): Call<GrantTokenResponse>

    @POST("/v1.2.0-beta/tokenized/checkout/create")
    fun postPaymentCreate(
        @Header("authorization") authorization: String?,
        @Header("x-app-key") xAppKey: String?,
        @Body createPaymentBody: CreatePaymentBodyRequest
    ): Call<CreatePaymentResponse>
}