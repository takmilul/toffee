package com.banglalink.toffee.data.network.interceptor

import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.di.CoilCache
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoilInterceptor @Inject constructor(@CoilCache val cache: Cache): Interceptor {

    // https://gist.github.com/danh32/d91f938dc223bd11da2f3310b3767020
    // https://github.com/square/okhttp/issues/6453

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            chain.proceed(request)
        }
        catch (e: Exception) {
            if (e.message?.contains("url", ignoreCase = true) == true) {
                val url = request.url
                val deleted = deleteCacheEntry(url)

                ToffeeAnalytics.logException(
                    IllegalArgumentException("Deleted -> $deleted, Invalid url: ${request.url}. Original msg -> ${e.message}")
                )
                // retry now that cache is deborked
                chain.proceed(request)
            } else {
                throw IOException(e.message)
            }
        }
    }

    private fun deleteCacheEntry(urlToDelete: HttpUrl): Boolean {
        // report non-fatal only after deletion, so logs include result
        return evictCorruptedCacheEntry(urlToDelete)
    }

    private fun evictCorruptedCacheEntry(httpUrl: HttpUrl): Boolean {
        val urlToDelete = httpUrl.toString()
        val iterator = cache.urls()
        var removed = false
        while (iterator.hasNext()) {
            val url = iterator.next()
            if (url == urlToDelete) {
                iterator.remove()
                removed = true
            }
        }

        if (!removed) {
            cache.evictAll()
        }

        return removed
    }
}