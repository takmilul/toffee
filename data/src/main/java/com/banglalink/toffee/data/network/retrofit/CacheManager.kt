package com.banglalink.toffee.data.network.retrofit

import coil.annotation.ExperimentalCoilApi
import coil.disk.DiskCache
import com.banglalink.toffee.di.CoilCache
import com.banglalink.toffee.di.DefaultCache
import okhttp3.Cache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheManager @Inject constructor(@DefaultCache private val retrofitCache: Cache, @CoilCache val coilCache: DiskCache) {
    fun clearCacheByUrl(apiUrl: String) {
        runCatching {
            val urlIterator = retrofitCache.urls()
            while (urlIterator.hasNext()) {
                if (urlIterator.next().contains(apiUrl.trim())) {
                    urlIterator.remove()
                }
            }
        }
    }
    
    fun clearAllCache() {
        runCatching {
            val urlIterator = retrofitCache.urls()
            while (urlIterator.hasNext()) {
                urlIterator.next()
                urlIterator.remove()
            }
        }
    }
    @OptIn(ExperimentalCoilApi::class)
    fun clearImageCacheByUrl(url: String) {
        runCatching {
            coilCache.remove(url)
        }
    }
    @OptIn(ExperimentalCoilApi::class)
    fun clearAllImageCache() {
        runCatching {
            coilCache.clear()
        }
    }
}