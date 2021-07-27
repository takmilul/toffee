package com.banglalink.toffee.data.network.interceptor

import com.banglalink.toffee.analytics.ToffeeAnalytics
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class CoilInterceptor: Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        try {
            return chain.proceed(request)
        }
        catch (ex: Exception) {
            if (ex is IllegalArgumentException) {
                ToffeeAnalytics.logException(
                    IllegalArgumentException("Invalid url: ${request.url}. Original msg -> ${ex.message}")
                )
            }
            throw IOException(ex.message)
        }
    }
}