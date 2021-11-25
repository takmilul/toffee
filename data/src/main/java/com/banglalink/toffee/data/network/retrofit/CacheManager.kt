package com.banglalink.toffee.data.network.retrofit

import com.banglalink.toffee.di.DefaultCache
import okhttp3.Cache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheManager @Inject constructor(@DefaultCache private val retrofitCache: Cache) {

    fun clearCacheByUrl(apiUrl: String) {
        val urlIterator = retrofitCache.urls()
        while (urlIterator.hasNext()) {
            if (urlIterator.next().contains(apiUrl.trim())) {
                urlIterator.remove()
                break
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