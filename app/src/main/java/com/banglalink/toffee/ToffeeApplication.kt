package com.banglalink.toffee

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.databinding.DataBindingUtil
import coil.Coil
import coil.ImageLoader
import coil.imageLoader
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.interceptor.CoilInterceptor
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.PlayerPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.di.CoilCache
import com.banglalink.toffee.di.databinding.CustomBindingComponentBuilder
import com.banglalink.toffee.di.databinding.CustomBindingEntryPoint
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.ui.upload.UploadObserver
import com.banglalink.toffee.usecase.SendFirebaseConnectionErrorEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.loopnow.fireworklibrary.FwSDK
import com.loopnow.fireworklibrary.SdkStatus
import com.loopnow.fireworklibrary.SdkStatus.*
import com.loopnow.fireworklibrary.VideoPlayerProperties
import com.medallia.digital.mobilesdk.MDExternalError
import com.medallia.digital.mobilesdk.MDResultCallback
import com.medallia.digital.mobilesdk.MedalliaDigital
import dagger.hilt.EntryPoints
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class ToffeeApplication : Application() {

    @Inject lateinit var cacheManager: CacheManager
    @Inject @CoilCache lateinit var coilCache: Cache
    @Inject lateinit var mUploadObserver: UploadObserver
    @Inject lateinit var coilInterceptor: CoilInterceptor
    @Inject lateinit var heartBeatManager: HeartBeatManager
    @Inject lateinit var commonPreference: CommonPreference
    @Inject lateinit var sessionPreference: SessionPreference
    private lateinit var connectivityManager: ConnectivityManager
    @Inject @AppCoroutineScope lateinit var coroutineScope: CoroutineScope
    @Inject lateinit var bindingComponentProvider: Provider<CustomBindingComponentBuilder>
    @Inject lateinit var sendFirebaseConnectionErrorEvent: SendFirebaseConnectionErrorEvent

    override fun onCreate() {
        super.onCreate()
        
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
        try{
            ToffeeAnalytics.initAppEventsLogger(this)
        }
        catch (e: Exception) {
        }
        
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
        
//        FacebookSdk.setIsDebugEnabled(true);
//        FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
        
        try {
            connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                    connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), heartBeatManager)
                } else {
                    Log.e("CONN_", "Watcher - Connectivity registration failed: network permission denied")
                }
            } else {
                connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), heartBeatManager)
            }
        } catch (e: Exception) {
            Log.e("CONN_", "Connectivity registration failed: ${e.message}")
        }
        
        initCoil()
        initFireworkSdk()
//        initMedalliaSdk()
        mUploadObserver.start()
    }
    
    private fun initFireworkSdk() {
        try {
            FwSDK.initialize(this, getString(R.string.firework_oauth_id), sessionPreference.getFireworkUserId(), object : FwSDK.SdkStatusListener{
                override fun currentStatus(status: SdkStatus, extra: String) {
                    when(status){
                        Initialized -> {
                            Log.e("FwSDK", "Initialized: $extra")
                            VideoPlayerProperties.share = false
                            VideoPlayerProperties.branding = false
                            VideoPlayerProperties.fullScreenPlayer = true
                            FwSDK.setBasePlayerUrl("https://toffeelive.com/")
                            sessionPreference.isFireworkInitialized.postValue(true)
                        }
                        InitializationFailed -> {
                            Log.e("FwSDK", "InitializationFailed: $extra")
                            ToffeeAnalytics.logException(java.lang.Exception("FwSDK InitializationFailed: $extra"))
                            sessionPreference.isFireworkInitialized.postValue(false)
                        }
                        LoadingContent -> Log.e("FwSDK", "LoadingContent: $extra")
                        ContentLoaded -> Log.e("FwSDK", "ContentLoaded: $extra")
                        LoadingContentFailed -> {
                            Log.e("FwSDK", "LoadingContentFailed: $extra")
                            ToffeeAnalytics.logException(java.lang.Exception("FwSDK LoadingContentFailed: $extra"))
                        }
                        RefreshTokenFailed -> Log.e("FwSDK", "RefreshTokenFailed: $extra")
                    }
                }
            })
        }
        catch (e: Exception) {
            Log.e("FwSDK", "onCreate: ${e.message}")
        }
    }
    
    private fun initMedalliaSdk() {
        MedalliaDigital.init(this, getString(R.string.medallia_api_key), object : MDResultCallback {
            override fun onSuccess() {
                Log.e("MED_", "onSuccess: Medallia initialized")
            }

            override fun onError(error: MDExternalError?) {
                ToffeeAnalytics.logException(Exception(error?.message))
                Log.e("MED_", "onError: ${error?.message}")
            }
        })
    }
    
    private fun initCoil() {
        val imageLoader = ImageLoader.Builder(this).apply {
            crossfade(true)
//            availableMemoryPercentage(0.2)
//            bitmapPoolPercentage(0.4)
            okHttpClient {
                OkHttpClient.Builder()
                    .cache(coilCache)
                    .addInterceptor(coilInterceptor)
                    .build()
            }
        }.build()
        Coil.setImageLoader(imageLoader)
    }

    override fun onTerminate() {
        try {
            connectivityManager.unregisterNetworkCallback(heartBeatManager)
        } catch (e: Exception) {
            ToffeeAnalytics.logBreadCrumb("connectivity manager unregister error -> ${e.message}")
        }
        super.onTerminate()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        with(imageLoader) {
            bitmapPool.clear()
            memoryCache.clear()
        }
    }
}

