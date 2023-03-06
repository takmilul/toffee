package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.data.network.request.CreatePaymentRequest
import com.banglalink.toffee.data.network.request.GrantTokenBodyRequest
import com.banglalink.toffee.data.network.response.CreatePaymentResponse
import com.banglalink.toffee.data.network.response.GrantTokenResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface ExternalApi {
    @POST
    suspend fun grantToken(
        @Url url: String?,
        @Header("username") bkashUsername: String?,
        @Header("password") bkashPassword: String?,
        @Body signup: GrantTokenBodyRequest
    ): GrantTokenResponse
    
    @POST
    suspend fun createPayment(
        @Url url: String?,
        @Header("authorization") authorization: String?,
        @Header("x-app-key") xAppKey: String?,
        @Body createPaymentBody: CreatePaymentRequest
    ): CreatePaymentResponse
}