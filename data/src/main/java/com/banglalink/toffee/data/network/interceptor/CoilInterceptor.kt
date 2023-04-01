package com.banglalink.toffee.data.network.interceptor

import coil.annotation.ExperimentalCoilApi
import coil.disk.DiskCache
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.CoilCache
import com.banglalink.toffee.extension.overrideUrl
import com.banglalink.toffee.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoilInterceptor @Inject constructor(
    @CoilCache val cache: DiskCache,
    val mPref: SessionPreference,
) : Interceptor {
    
    // https://gist.github.com/danh32/d91f938dc223bd11da2f3310b3767020
    // https://github.com/square/okhttp/issues/6453
    
    @Throws(IOException::class)
    @OptIn(ExperimentalCoilApi::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            val newRequest = if (mPref.shouldOverrideImageHostUrl) {
                val url = request.url.toUrl().toString().overrideUrl(mPref.overrideImageHostUrl)
                request.newBuilder().url(url).build()
            } else request
            chain.proceed(newRequest)
        } catch (e: Exception) {
            if (e.message?.contains("url", ignoreCase = true) == true) {
                val url = request.url
                val deleted = cache.remove(url.toString())
                
                ToffeeAnalytics.logException(
                    IllegalArgumentException("Deleted -> $deleted, Invalid url: ${request.url}. Original msg -> ${e.message}")
                )
                
                chain.proceed(request)
            } else {
                throw IOException(e.message)
            }
        }//.newBuilder().addHeader("Cache-Control", "public, max-age=604800").removeHeader("Pragma").build()
        
        if (response.cacheResponse != null) {
            Log.i("IMAGE_LOG", "FROM CACHE")
        }
        if (response.networkResponse != null) {
            Log.i("IMAGE_LOG", "FROM NETWORK: ${response.request.url}")
        }
        
        return response
    }
}