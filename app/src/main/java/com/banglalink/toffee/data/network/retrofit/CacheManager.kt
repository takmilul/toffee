package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.apiservice.GET_ALL_CHANNELS
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_DETAILS
import com.banglalink.toffee.apiservice.GET_TRENDING_CHANNELS
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
            if (next.contains(GET_ALL_CHANNELS) || next.contains(GET_TRENDING_CHANNELS) || next.contains
                    (GET_MY_CHANNEL_DETAILS)) {
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