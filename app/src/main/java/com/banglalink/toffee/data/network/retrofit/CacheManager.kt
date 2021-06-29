package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.di.DefaultCache
import okhttp3.Cache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheManager @Inject constructor(@DefaultCache private val retrofitCache: Cache) {

    fun clearCacheByUrl(apiUrl: String) {
        val urlIterator = retrofitCache.urls()
        while (urlIterator.hasNext()) {
            if (urlIterator.next().contains(apiUrl)) {
                urlIterator.remove()
            }
        }
    }
    
    fun clearSubscriptionCache() {
        val urlIterator = retrofitCache.urls()
        while (urlIterator.hasNext()) {
            val next = urlIterator.next()
            if (next.contains(ApiRoutes.GET_ALL_CHANNELS) || next.contains(ApiRoutes.GET_TRENDING_CHANNELS) || next.contains
                    (ApiRoutes.GET_MY_CHANNEL_DETAILS) || next.contains(ApiRoutes.GET_SUBSCRIBED_CHANNELS)) {
                urlIterator.remove()
            }
        }
    }
    
    fun clearAllCache(){
        val urlIterator = retrofitCache.urls()
        while (urlIterator.hasNext()) {
            urlIterator.next()
            urlIterator.remove()
        }
    }
}