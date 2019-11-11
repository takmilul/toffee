package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.data.network.request.ApiLoginRequest
import com.banglalink.toffee.data.network.request.CheckUpdateRequest
import com.banglalink.toffee.data.network.response.ApiLoginResponse
import com.banglalink.toffee.data.network.response.CheckUpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi{

    @POST("check-for-update")
    suspend fun checkForUpdate(checkUpdateRequest: CheckUpdateRequest): Response<CheckUpdateResponse>

    @POST("api-login")
    suspend fun apiLogin(@Body apiLoginRequest: ApiLoginRequest): Response<ApiLoginResponse>
}