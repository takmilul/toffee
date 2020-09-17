package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.BuildConfig
import com.banglalink.toffee.data.network.request.ApiLoginRequest
import com.banglalink.toffee.data.network.request.CheckUpdateRequest
import com.banglalink.toffee.data.network.response.ApiLoginResponse
import com.banglalink.toffee.data.network.response.CheckUpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi{

    @POST("check-for-update")
    suspend fun checkForUpdate(@Body checkUpdateRequest: CheckUpdateRequest): CheckUpdateResponse

    @POST("check-for-update-v2/Android/${BuildConfig.VERSION_CODE}/1/0")
    suspend fun checkForUpdateV2(@Body checkUpdateRequest: CheckUpdateRequest): CheckUpdateResponse

    @POST("api-login")
    suspend fun apiLogin(@Body apiLoginRequest: ApiLoginRequest): ApiLoginResponse
}