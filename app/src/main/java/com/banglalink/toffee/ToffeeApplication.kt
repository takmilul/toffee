package com.banglalink.toffee

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import coil.Coil
import coil.ImageLoader
import coil.util.CoilUtils
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import com.banglalink.toffee.ui.upload.UploadObserver
import dagger.hilt.android.HiltAndroidApp
import net.gotev.uploadservice.UploadServiceConfig
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltAndroidApp
class ToffeeApplication : Application() {

    @Inject lateinit var mUploadObserver: UploadObserver

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
        PubSubMessageUtil.init(this)
        Preference.init(this)
        ToffeeAnalytics.initFireBaseAnalytics(this)
        
        initTheme()
        initCoil()

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), HeartBeatManager)


        initUploader()
    }

    private fun initTheme() {
        when (Preference.getInstance().appThemeMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun initCoil() {
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

    private fun initUploader() {

        createNotificationChannel()

        UploadServiceConfig.initialize(this, notificationChannelID, BuildConfig.DEBUG)
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

