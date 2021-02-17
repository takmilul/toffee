package com.banglalink.toffee

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.imageLoader
import coil.util.CoilUtils
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.ui.upload.UploadObserver
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import net.gotev.uploadservice.UploadServiceConfig
import net.gotev.uploadservice.data.RetryPolicyConfig
import net.gotev.uploadservice.okhttp.OkHttpStack
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class ToffeeApplication : Application() {

    @Inject lateinit var mUploadObserver: UploadObserver
    @Inject lateinit var heartBeatManager: HeartBeatManager

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
        PubSubMessageUtil.init(this)
        Preference.init(this)
        ToffeeAnalytics.initFireBaseAnalytics(this)
        
        initCoil()

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), heartBeatManager)


        initUploader()
    }

    private fun initCoil() {
        val imageLoader = ImageLoader.Builder(this).apply {
            crossfade(true)
//            availableMemoryPercentage(0.2)
//            bitmapPoolPercentage(0.4)
            okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(this@ToffeeApplication))
                    .build()
            }

        }.build()
        Coil.setImageLoader(imageLoader)
    }

    override fun onTerminate() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(heartBeatManager)
        super.onTerminate()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        with(imageLoader) {
            bitmapPool.clear()
            memoryCache.clear()
        }
    }

    private fun initUploader() {

        createNotificationChannel()

        UploadServiceConfig.initialize(this, notificationChannelID, BuildConfig.DEBUG)
        UploadServiceConfig.httpStack = OkHttpStack()
        UploadServiceConfig.retryPolicy = RetryPolicyConfig(
            initialWaitTimeSeconds = 5,
            maxWaitTimeSeconds = 30,
            multiplier = 1,
            defaultMaxRetries = 6
        )
        mUploadObserver.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                notificationChannelID,
                "Toffee Upload Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val notificationChannelID = "Toffee Upload"
    }
}

