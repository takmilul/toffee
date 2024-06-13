package com.banglalink.toffee.data.network.interceptor

import com.banglalink.toffee.data.exception.AuthEncodeDecodeException
import com.banglalink.toffee.data.exception.AuthInterceptorException
import com.banglalink.toffee.data.network.response.ExternalBaseResponse
import com.banglalink.toffee.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlainInterceptor @Inject constructor(
    private val json: Json
) : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
//        Log.i("Path", request.url.encodedPath)
        val newRequest = request.newBuilder()
            .headers(request.headers)
            
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
            var body = responseJsonString?.toResponseBody(contentType)
            if (!response.isSuccessful) {
                val errorBody = try {
                    json.decodeFromString<ExternalBaseResponse>(responseJsonString ?: "")
                } catch (ex: Exception) {
                    val customErrorBody = ExternalBaseResponse().apply {
                        errorCode = 400
                        errorMsg = responseJsonString
                    }
                    body = json.encodeToString(customErrorBody).toResponseBody(contentType)
                    customErrorBody
                }
                return response.newBuilder()
                    .code(200)
                    .body(body)
                    .message(errorBody.errorMsg ?: "Something went wrong. Please try again.")
                    .removeHeader("Pragma")
                    .build()
            }
            
            return response.newBuilder().body(body).build()
        } catch (e: Exception) {
            throw AuthEncodeDecodeException(e.message, e.cause)
        }
    }
}