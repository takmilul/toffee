package com.banglalink.toffee.data.network.interceptor

import com.banglalink.toffee.Constants.CLIENT_API_HEADER
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.exception.AuthEncodeDecodeException
import com.banglalink.toffee.data.exception.AuthInterceptorException
import com.banglalink.toffee.data.network.request.BaseRequest
import com.banglalink.toffee.data.network.response.BaseResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.ApiHeader
import com.banglalink.toffee.di.AppVersionCode
import com.banglalink.toffee.di.AppVersionName
import com.banglalink.toffee.di.ApplicationId
import com.banglalink.toffee.di.ToffeeHeader
import com.banglalink.toffee.extension.isNotNullOrBlank
import com.banglalink.toffee.extension.overrideUrl
import com.banglalink.toffee.lib.BuildConfig
import com.banglalink.toffee.util.EncryptionUtil
import com.banglalink.toffee.util.Log
import kotlinx.serialization.json.Json
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
    private val json: Json,
    private val mPref: SessionPreference,
    @ApplicationId private val appId: String,
    @AppVersionCode private val appVersionCode: Int,
    @AppVersionName private val appVersionName: String,
    private val iGetMethodTracker: IGetMethodTracker,
    @ApiHeader private val apiUserAgent: Provider<String>,
    @ToffeeHeader private val playerUserAgent: Provider<String>,
) : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        ToffeeAnalytics.logBreadCrumb("apiName: ${request.url.encodedPath.substringAfter("/").substringBefore("/")}")
        val convertToGet = iGetMethodTracker.shouldConvertToGetRequest(request.url.encodedPath)
        val builder = FormBody.Builder()
        val requestJsonString = bodyToString(request.body)
        val baseRequest = try { json.decodeFromString<BaseRequest>(requestJsonString) } catch (e: Exception) { null }
//        if (baseRequest != null && (!baseRequest.osVersion.contains("android", true) || baseRequest.appVersion != BuildConfig.APP_VERSION_NAME || appId != "com.banglalink.toffee" || appVersionCode != BuildConfig.APP_VERSION_CODE || appVersionName != BuildConfig.APP_VERSION_NAME)) {
//            throw IOException()
//        }
        
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
        
        if (response.cacheResponse != null) {
            Log.i("API_CACHING_LOG", "FROM CACHE  : ${response.request.url}")
        }
        if (response.networkResponse != null) {
            Log.i("API_CACHING_LOG", "FROM NETWORK: ${response.request.url}")
        }
        
        try {
            val isFromCacheJson = ",\"isFromCache\":${response.cacheResponse != null}"
            val responseJsonString = EncryptionUtil.decryptResponse(response.body!!.string()).isNotNullOrBlank {
                it.replaceRange(it.lastIndex, it.lastIndex, isFromCacheJson)
            } ?: ""
            ToffeeAnalytics.logBreadCrumb("response: $responseJsonString")
            val contentType = response.body!!.contentType()
            val body = responseJsonString.toResponseBody(contentType)
            
            if (!response.isSuccessful) {
                return if (response.code == 403) {
                    val msg = "Attention! Toffee is available only within Bangladesh territory. Please use a Bangladesh IP to access."
                    response.newBuilder()
                        .code(response.code)
                        .message(msg)
                        .build()
                } else {
                    val errorBody = json.decodeFromString<BaseResponse>(responseJsonString)
                    response.newBuilder()
                        .code(200)
                        .body(body)
                        .message(errorBody.errorMsg ?: "Something went wrong. Please try again.")
                        .removeHeader("Pragma")
                        .build()
                }
            }
            
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