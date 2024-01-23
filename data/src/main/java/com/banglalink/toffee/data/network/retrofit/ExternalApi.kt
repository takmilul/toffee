package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.data.network.request.CreatePaymentRequest
import com.banglalink.toffee.data.network.request.ExecutePaymentRequest
import com.banglalink.toffee.data.network.request.GrantTokenRequest
import com.banglalink.toffee.data.network.request.KabbikLoginApiRequest
import com.banglalink.toffee.data.network.request.QueryPaymentRequest
import com.banglalink.toffee.data.network.response.AudioBookEpisodeResponse
import com.banglalink.toffee.data.network.response.CreatePaymentResponse
import com.banglalink.toffee.data.network.response.ExecutePaymentResponse
import com.banglalink.toffee.data.network.response.GrantTokenResponse
import com.banglalink.toffee.data.network.response.KabbikHomeApiResponse
import com.banglalink.toffee.data.network.response.KabbikLoginApiResponse
import com.banglalink.toffee.data.network.response.KabbikTopBannerApiResponse
import com.banglalink.toffee.data.network.response.QueryPaymentResponse
import com.banglalink.toffee.data.network.response.AudioBookSeeMoreResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface ExternalApi {
    @POST
    suspend fun grantToken(
        @Url url: String?,
        @Header("username") bkashUsername: String?,
        @Header("password") bkashPassword: String?,
        @Body grantTokenRequest: GrantTokenRequest
    ): GrantTokenResponse
    
    @POST
    suspend fun createPayment(
        @Url url: String?,
        @Header("authorization") authorization: String?,
        @Header("x-app-key") xAppKey: String?,
        @Body createPaymentBody: CreatePaymentRequest
    ): CreatePaymentResponse

    @POST
    suspend fun executePayment(
        @Url url: String?,
        @Header("authorization") authorization: String?,
        @Header("x-app-key") xAppKey: String?,
        @Body executePaymentRequest: ExecutePaymentRequest
    ): ExecutePaymentResponse

    @POST
    suspend fun statusPayment(
        @Url url: String?,
        @Header("authorization") authorization: String?,
        @Header("x-app-key") xAppKey: String?,
        @Body queryPaymentRequest: QueryPaymentRequest
    ): QueryPaymentResponse

    /* Kabbik Audio Book and Podcasts */
    @POST
    suspend fun kabbikLoginApi(
        @Url url: String?,
        @Header("Referrer") referrer: String?,
        @Body request: KabbikLoginApiRequest
    ): KabbikLoginApiResponse

    @GET
    suspend fun kabbikHomeApi(
        @Url url: String?,
        @Header("Authorization") token : String,
        @Header("Referrer") referrer: String?,
        ) : KabbikHomeApiResponse

    @GET
    suspend fun kabbikTopBanner(
        @Url url: String?,
        @Header("Authorization") token : String,
        @Header("Referrer") referrer: String?,
        ): KabbikTopBannerApiResponse

    @GET
    suspend fun audioBookSeeMoreList(
        @Url url: String?,
        @Header("Authorization") authorizationToken: String?,
        @Header("Referrer") referrer: String?,
        @Query("name") name: String,
    ): AudioBookSeeMoreResponse

    @GET
    suspend fun audioBookEpisodeList(
        @Url url: String?,
        @Header("Authorization") authorizationToken: String?,
        @Header("Referrer") referrer: String?,
    ): AudioBookEpisodeResponse
}