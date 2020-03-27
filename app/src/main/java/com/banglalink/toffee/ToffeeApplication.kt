package com.banglalink.toffee

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import coil.Coil
import coil.ImageLoader
import coil.util.CoilUtils
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.Preference
import okhttp3.OkHttpClient



class ToffeeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Preference.init(this)
        ToffeeAnalytics.initFireBaseAnalytics(this)

        val imageLoader = ImageLoader(this) {
            crossfade(true)
            bitmapPoolPercentage(0.3)
            okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(this@ToffeeApplication))
                    .build()
            }

        }

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), HeartBeatManager)

        Coil.setDefaultImageLoader(imageLoader)

    }

    override fun onTerminate() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(HeartBeatManager)
        super.onTerminate()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Coil.loader().clearMemory()
    }
}

