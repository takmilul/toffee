package com.banglalink.toffee

import android.app.Application
import androidx.databinding.DataBindingUtil
import coil.Coil
import coil.ImageLoader
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.PlayerPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.di.CoilImageLoader
import com.banglalink.toffee.di.databinding.CustomBindingComponentBuilder
import com.banglalink.toffee.di.databinding.CustomBindingEntryPoint
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.ui.upload.UploadObserver
import com.banglalink.toffee.usecase.SendFirebaseConnectionErrorEvent
import com.banglalink.toffee.util.Log
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
import javax.inject.Inject
import javax.inject.Provider

@HiltAndroidApp
class ToffeeApplication : Application() {
    
    @Inject lateinit var cacheManager: CacheManager
    @Inject lateinit var mUploadObserver: UploadObserver
    @Inject lateinit var heartBeatManager: HeartBeatManager
    @Inject lateinit var commonPreference: CommonPreference
    @Inject lateinit var sessionPreference: SessionPreference
    @Inject @CoilImageLoader lateinit var imageLoader: ImageLoader
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
        Coil.setImageLoader(imageLoader)
        try {
            ToffeeAnalytics.initFireBaseAnalytics(this)
        } catch (e: Exception) {
            coroutineScope.launch {
                sendFirebaseConnectionErrorEvent.execute()
            }
        }
        try {
            ToffeeAnalytics.initFacebookAnalytics(this)
        } catch (e: Exception) {
        }
        
        if (commonPreference.versionCode < BuildConfig.VERSION_CODE) {
            try {
                coroutineScope.launch(IO) {
                    cacheManager.clearAllCache()
                    commonPreference.versionCode = BuildConfig.VERSION_CODE
                }
            } catch (e: Exception) {
                ToffeeAnalytics.logException(e)
            }
        }

//        FacebookSdk.setIsDebugEnabled(true);
//        FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
        
        initFireworkSdk()
        initMedalliaSdk()
        mUploadObserver.start()
    }
    
    private fun initFireworkSdk() {
        try {
            FwSDK.initialize(this, getString(R.string.firework_oauth_id), sessionPreference.getFireworkUserId(), object : FwSDK.SdkStatusListener {
                override fun currentStatus(status: SdkStatus, extra: String) {
                    when (status) {
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
        } catch (e: Exception) {
            Log.e("FwSDK", "onCreate: ${e.message}")
        }
    }
    
    private fun initMedalliaSdk() {
        try {
            MedalliaDigital.init(this, getString(R.string.medallia_api_key), object : MDResultCallback {
                override fun onSuccess() {
                    Log.i("MED_", "onSuccess: Medallia initialized")
                }
                
                override fun onError(error: MDExternalError?) {
                    Log.e("MED_", "onError: ${error?.message}")
                }
            })
        } catch (e: Exception) {
            Log.e("MED_", "onInitialize: ${e.message}")
        }
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        with(imageLoader) {
            bitmapPool.clear()
            memoryCache.clear()
        }
    }
}

