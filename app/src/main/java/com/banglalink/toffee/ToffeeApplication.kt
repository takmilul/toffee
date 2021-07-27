package com.banglalink.toffee

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import androidx.databinding.DataBindingUtil
import coil.Coil
import coil.ImageLoader
import coil.imageLoader
import coil.util.CoilUtils
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.interceptor.CoilInterceptor
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.PlayerPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.di.databinding.CustomBindingComponentBuilder
import com.banglalink.toffee.di.databinding.CustomBindingEntryPoint
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.ui.upload.UploadObserver
import com.banglalink.toffee.usecase.SendFirebaseConnectionErrorEvent
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.EntryPoints
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import net.gotev.uploadservice.UploadServiceConfig
import net.gotev.uploadservice.data.RetryPolicyConfig
import net.gotev.uploadservice.okhttp.OkHttpStack
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class ToffeeApplication : Application() {

    @Inject lateinit var cacheManager: CacheManager
    @Inject lateinit var mUploadObserver: UploadObserver
    @Inject lateinit var commonPreference: CommonPreference
    @Inject lateinit var heartBeatManager: HeartBeatManager
    @Inject @AppCoroutineScope lateinit var coroutineScope: CoroutineScope
    @Inject lateinit var bindingComponentProvider: Provider<CustomBindingComponentBuilder>
    @Inject lateinit var sendFirebaseConnectionErrorEvent: SendFirebaseConnectionErrorEvent

    override fun onCreate() {
        super.onCreate()

        if (commonPreference.versionCode < BuildConfig.VERSION_CODE) {
            try {
                coroutineScope.launch(IO) { 
                    cacheManager.clearAllCache()
                    commonPreference.versionCode = BuildConfig.VERSION_CODE
                }
            }
            catch (e: Exception) {
                ToffeeAnalytics.logException(e)
            }
        }
        
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }

        // (Binding adapter with hilt) https://gist.github.com/nuhkoca/1bf28190dc71b00a2f32ce425f99924d
        val dataBindingComponent = bindingComponentProvider.get().build()
        val dataBindingEntryPoint = EntryPoints.get(
            dataBindingComponent, CustomBindingEntryPoint::class.java
        )
        DataBindingUtil.setDefaultComponent(dataBindingEntryPoint)

        PubSubMessageUtil.init(this)
        SessionPreference.init(this)
        CommonPreference.init(this)
        PlayerPreference.init(this)
        try {
            ToffeeAnalytics.initFireBaseAnalytics(this)
        }

        catch (e: Exception) {
            coroutineScope.launch { 
                sendFirebaseConnectionErrorEvent.execute()
            }
        }

        FacebookSdk.setIsDebugEnabled(true);
        FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);

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
                    .addInterceptor(CoilInterceptor())
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

