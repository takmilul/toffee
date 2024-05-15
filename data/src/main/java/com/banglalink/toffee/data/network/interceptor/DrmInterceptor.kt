package com.banglalink.toffee.data.network.interceptor

import com.banglalink.toffee.data.exception.AuthEncodeDecodeException
import com.banglalink.toffee.data.exception.AuthInterceptorException
import com.banglalink.toffee.data.network.response.DrmLicenseResponse
import com.banglalink.toffee.util.Log
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.userAgent
import java.io.IOException
import javax.inject.Singleton

@Singleton
class DrmInterceptor() : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
//        val builder = FormBody.Builder()
        val body = "{" +
            "\"payload\": \"CAQ=\"," +
            "\"drmType\": \"WV\"," +
            "\"contentId\": \"1\"," +
            "\"providerId\": \"toffee\"," +
            "\"packageId\": \"1\"," +
            "\"token\": \"dummy\"" +
            "}"
//        builder.addEncoded("data", body)
        
        val newRequest = request.newBuilder()
            .headers(request.headers)
            .addHeader("User-Agent", userAgent)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), body))
//            .method("POST", builder.build())
        
        val response = try {
            chain.proceed(newRequest.build())
        } catch (ex: IOException) {
            throw ex
        } catch (ex: Exception) {
            throw AuthInterceptorException(ex.message, ex.cause)
        }
        
        Log.d("_header", "intercept: "+response.headers)
        if (response.cacheResponse != null) {
            Log.i("API_LOG", "FROM CACHE : ${response.request.url}")
        }
        if (response.networkResponse != null) {
            Log.i("API_LOG", "FROM NETWORK : ${response.request.url}")
        }
        try {
            val responseJsonString = response.body?.string()
            val contentType = response.body?.contentType()
            val responseJson = Json.decodeFromString<DrmLicenseResponse>(responseJsonString!!).data?.payload
            val newBody = responseJson?.toResponseBody(contentType)
            
            return response.newBuilder().body(newBody).build()
        } catch (e: Exception) {
            throw AuthEncodeDecodeException(e.message, e.cause)
        }
    }
}