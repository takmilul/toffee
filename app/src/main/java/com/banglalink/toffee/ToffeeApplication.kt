package com.banglalink.toffee

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.util.CoilUtils
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.util.Utils
import okhttp3.OkHttpClient



class ToffeeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Preference.init(this)

        val imageLoader = ImageLoader(this) {
            crossfade(true)
            bitmapPoolPercentage(0.3)
            okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(this@ToffeeApplication))
                    .build()
            }

        }

        Coil.setDefaultImageLoader(imageLoader)
        Utils.disableSSLCertificateVerify()

    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Coil.loader().clearMemory()
    }
}

