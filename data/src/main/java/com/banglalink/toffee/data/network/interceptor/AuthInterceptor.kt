package com.banglalink.toffee.data.network.interceptor

import com.banglalink.toffee.Constants.CLIENT_API_HEADER
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.exception.AuthEncodeDecodeException
import com.banglalink.toffee.data.exception.AuthInterceptorException
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.ApiHeader
import com.banglalink.toffee.di.ToffeeHeader
import com.banglalink.toffee.extension.overrideUrl
import com.banglalink.toffee.util.EncryptionUtil
import com.banglalink.toffee.util.Log
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val mPref: SessionPreference,
    private val iGetMethodTracker: IGetMethodTracker,
    @ApiHeader private val apiUserAgent: Provider<String>,
    @ToffeeHeader private val playerUserAgent: Provider<String>
) : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
//        Log.i("Path", request.url.encodedPath)
        ToffeeAnalytics.logBreadCrumb("apiName: ${request.url.encodedPath.substringAfter("/").substringBefore("/")}")
        val convertToGet = iGetMethodTracker.shouldConvertToGetRequest(request.url.encodedPath)
        val builder = FormBody.Builder()
        val requestJsonString = bodyToString(request.body)
        ToffeeAnalytics.logBreadCrumb("request: $requestJsonString")
        val string = EncryptionUtil.encryptRequest(requestJsonString)
        if (!convertToGet) {
            builder.addEncoded("data", string)
        }
        
//        Log.i("Agent", "apiAgent: ${System.getProperty("http.agent")}\n\n")
        val userAgent = if (request.url.toString().contains("bl-he")) {
            System.getProperty("http.agent") ?: apiUserAgent.get()
        } else {
            playerUserAgent.get()
        }
        val newRequest = request.newBuilder()
            .headers(request.headers)
            .addHeader("User-Agent", userAgent)
            .method(if (convertToGet) "GET" else "POST", if (convertToGet) null else builder.build()).apply {
                if (mPref.shouldOverrideBaseUrl) {
                    url(request.url.toUrl().toString().overrideUrl(mPref.overrideBaseUrl))
                }
            }
        if (convertToGet) {
            newRequest.addHeader(CLIENT_API_HEADER, string)
        }
        
        val response = try {
            chain.proceed(newRequest.build())
        } catch (ex: IOException) {
            throw ex
        } catch (ex: Exception) {
            throw AuthInterceptorException(ex.message, ex.cause)
        }
        if (!response.isSuccessful) {
            if (response.code == 403) {
                val msg = "Attention! Toffee is available only within Bangladesh territory. Please use a Bangladesh IP to access."
                return response.newBuilder()
                    .code(response.code)
                    .message(msg)
                    .build()
            }
            return response.newBuilder().removeHeader("Pragma").build()
        }
        if (response.cacheResponse != null) {
            Log.i("Network", "FROM CACHE")
        }
        if (response.networkResponse != null) {
            Log.i("Network", "FROM NETWORK")
        }
        try {
            val isFromCacheJson = ",\"isFromCache\":${response.cacheResponse != null}"
            val responseJsonString = EncryptionUtil.decryptResponse(response.body!!.string()).let {
                it.replaceRange(it.lastIndex, it.lastIndex, isFromCacheJson)
            }
            ToffeeAnalytics.logBreadCrumb("response: $responseJsonString")
            val contentType = response.body!!.contentType()
            val body = responseJsonString.toResponseBody(contentType)
            return response.newBuilder().body(body).build()
        } catch (e: Exception) {
            throw AuthEncodeDecodeException(e.message, e.cause)
        }
    }
    
    private fun bodyToString(request: RequestBody?): String {
        val buffer = Buffer()
        try {
            if (request != null)
                request.writeTo(buffer)
            else
                return ""
            return buffer.readUtf8()
        } catch (e: IOException) {
            return ""
        } finally {
            buffer.close()
        }
    }
}