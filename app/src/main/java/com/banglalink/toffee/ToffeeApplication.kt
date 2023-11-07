package com.banglalink.toffee

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.databinding.DataBindingUtil
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.request.CachePolicy.DISABLED
import coil.request.CachePolicy.ENABLED
import coil.request.ImageRequest
import com.banglalink.toffee.analytics.HeartBeatManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.PlayerPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.AppCoroutineScope
import com.banglalink.toffee.di.CoilCache
import com.banglalink.toffee.di.CoilHttpClient
import com.banglalink.toffee.di.CustomCookieManager
import com.banglalink.toffee.di.databinding.CustomBindingComponentBuilder
import com.banglalink.toffee.di.databinding.CustomBindingEntryPoint
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.ui.bubble.BaseBubbleService
import com.banglalink.toffee.ui.upload.UploadObserver
import com.banglalink.toffee.usecase.SendFirebaseConnectionErrorEvent
import com.banglalink.toffee.util.Log
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.loopnow.fireworklibrary.FwSDK
import com.loopnow.fireworklibrary.SdkStatus
import com.loopnow.fireworklibrary.SdkStatus.InitializationFailed
import com.loopnow.fireworklibrary.SdkStatus.Initialized
import com.loopnow.fireworklibrary.SdkStatus.RefreshTokenFailed
import com.loopnow.fireworklibrary.VideoPlayerProperties
import com.medallia.digital.mobilesdk.MDExternalError
import com.medallia.digital.mobilesdk.MDResultCallback
import com.medallia.digital.mobilesdk.MedalliaDigital
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.microsoft.clarity.models.LogLevel
import dagger.hilt.EntryPoints
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import javax.inject.Inject
import javax.inject.Provider
import javax.net.ssl.SSLContext

@HiltAndroidApp
class ToffeeApplication : Application(), ImageLoaderFactory, Configuration.Provider {
    
    @Inject lateinit var cacheManager: CacheManager
    @Inject @CoilCache lateinit var coilCache: DiskCache
    @Inject lateinit var mUploadObserver: UploadObserver
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var commonPreference: CommonPreference
    @Inject lateinit var heartBeatManager: HeartBeatManager
    @Inject lateinit var sessionPreference: SessionPreference
    private lateinit var connectivityManager: ConnectivityManager
    @Inject @CoilHttpClient lateinit var coilHttpClient: OkHttpClient
    @Inject @AppCoroutineScope lateinit var coroutineScope: CoroutineScope
    @CustomCookieManager @Inject lateinit var defaultCookieManager: CookieManager
    @Inject lateinit var bindingComponentProvider: Provider<CustomBindingComponentBuilder>
    @Inject lateinit var sendFirebaseConnectionErrorEvent: SendFirebaseConnectionErrorEvent
    
    /**
     * [<b>On Application launch, doing the following actions:</b>]
     * 
     * &bull; [FirebaseApp] Initialized.
     * 
     * &bull;  Disabled [FirebaseCrashlytics] when run the app in debug mode.
     * 
     * &bull; Firebase Analytics Initialized. [ToffeeAnalytics.initFirebaseAnalytics]
     * 
     * &bull; Facebook Analytics Initialized. [ToffeeAnalytics.initFacebookAnalytics]
     * 
     * &bull; Install latest OpenSSL using Google Play.
     * 
     * &bull; DataBinding adapter injection using Hilt.
     * [<a href="https://gist.github.com/nuhkoca/1bf28190dc71b00a2f32ce425f99924d">Reference</a>]
     * 
     * &bull; Clear all API Cache when the app updated from Play Store to avoid any caching issue in the new implementation.
     * 
     * &bull; All Custom [android.content.SharedPreferences] Initialization. [SessionPreference], [CommonPreference], [PlayerPreference]
     * 
     * &bull; [PubSubMessageUtil] Initialized for sending app logs using Google's
     * [<a href="https://cloud.google.com/pubsub/docs/publish-receive-messages-client-library">Pub/Sub</a>]
     * 
     * &bull; Registers [ConnectivityManager] and implemented [ConnectivityManager.NetworkCallback] in [HeartBeatManager] to call Heartbeat API
      on Network change
     * 
     * &bull; Firework SDK Initialized. [FwSDK]
     * 
     * &bull; [MedalliaDigital] SDK Initialized.
     * 
     * &bull; [UploadObserver.start] called
     */
    override fun onCreate() {
        super.onCreate()
        
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER)
        if (CookieHandler.getDefault() !== defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager)
        }
        
        FirebaseApp.initializeApp(this).apply {
            if (BuildConfig.DEBUG) {
                FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
            }
        }
        
        try {
            ToffeeAnalytics.initFirebaseAnalytics(this)
        } catch (e: Exception) {
            coroutineScope.launch {
                sendFirebaseConnectionErrorEvent.execute()
            }
        }
        
        try {
            ToffeeAnalytics.initFacebookAnalytics(this)
        } catch (_: Exception) { }
        
        try {
            // Google Play will install latest OpenSSL 
            ProviderInstaller.installIfNeeded(applicationContext)
            val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, null, null)
            sslContext.createSSLEngine()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // (Binding adapter with hilt) https://gist.github.com/nuhkoca/1bf28190dc71b00a2f32ce425f99924d
        val dataBindingComponent = bindingComponentProvider.get().build()
        val dataBindingEntryPoint = EntryPoints.get(
            dataBindingComponent, CustomBindingEntryPoint::class.java
        )
        DataBindingUtil.setDefaultComponent(dataBindingEntryPoint)
        
        if (commonPreference.versionCode < BuildConfig.VERSION_CODE) {
            try {
                sessionPreference.bubbleDialogShowCount = 0
                coroutineScope.launch(IO) {
                    cacheManager.clearAllCache()
                    commonPreference.versionCode = BuildConfig.VERSION_CODE
                }
            } catch (e: Exception) {
                ToffeeAnalytics.logException(e)
            }
        }
        
        SessionPreference.init(this)
        CommonPreference.init(this)
        PlayerPreference.init(this)
        PubSubMessageUtil.init(this)
        
        try {
            connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED) {
                    connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), heartBeatManager)
                } else {
                    Log.e("CONN_", "Connectivity registration failed: network permission denied")
                }
            } else {
                connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), heartBeatManager)
            }
        } catch (e: Exception) {
            Log.e("CONN_", "Connectivity registration failed: ${e.message}")
        }
        
//        FacebookSdk.setIsDebugEnabled(true);
//        FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);

        initFireworkSdk()
        initMedalliaSdk()
        mUploadObserver.start()
        BaseBubbleService.isForceClosed = false
    }
    
    /**
     * [androidx.work.WorkManager] configured in the application level to use in the full application.
     */
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().setMinimumLoggingLevel(android.util.Log.INFO).setWorkerFactory(workerFactory).build()
    }
    
    /**
     * [<b>This SDK shows Shorts Video Reels</b>]
     * 
     * To initialize [FwSDK], we need to pass applicationContext, oAuthId provided by Firework, an unique ID as UserId and [com.loopnow.fireworklibrary.FwSDK.SdkStatusListener] to determine the initialization success or failure state.
     * 
     * if the SDK initializes successfully, we added some basic configuration with the SDK with a live data flag to show the Firework Reel in 
      the home page
     * 
     * if the SDK failed to initialize, we logged the exception and set a flag with a live data not to show the Firework Reel in the home page
     */
    private fun initFireworkSdk() {
        try {
            FwSDK.initialize(this, BuildConfig.FIREWORK_OAUTH_ID, sessionPreference.getFireworkUserId(), object : FwSDK.SdkStatusListener {
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
                            ToffeeAnalytics.logException(Exception("FwSDK InitializationFailed: $extra"))
                            sessionPreference.isFireworkInitialized.postValue(false)
                        }
                        RefreshTokenFailed -> Log.e("FwSDK", "RefreshTokenFailed: $extra")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("FwSDK", "onCreate: ${e.message}")
        }
    }
    
    /**
     * [<b>This SDK shows a popup form to take users feedback</b>]
     * 
     * To initialize [MedalliaDigital] SDK, we need to pass applicationContext, ApiKey provided by Medallia and [MDResultCallback] to detect 
      the success and error state
     */
    private fun initMedalliaSdk() {
        try {
            MedalliaDigital.init(this, BuildConfig.MEDALLIA_API_KEY, object : MDResultCallback {
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
    
    /**
     * [<b>This code configures settings for kotlin's `coil` image loader library</b>]
     * 
     * Create image loader instance for [coil] library. here we configured the image loader for the whole app. 
     */
    override fun newImageLoader(): ImageLoader {
        val imageRequest = ImageRequest.Builder(this).apply {
            dispatcher(IO)
            crossfade(750)
            diskCachePolicy(ENABLED)
            networkCachePolicy(ENABLED)
            memoryCachePolicy(DISABLED)
            allowHardware(false)
        }.build()
        
        return ImageLoader.Builder(this).apply {
//            logger(DebugLogger())
            components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            diskCache {
                coilCache
            }
            okHttpClient {
                coilHttpClient
            }
        }.build().apply {
            enqueue(imageRequest)
        }
    }
    
    /**
     * [<b>Calls when app is terminated</b>]
     * 
     * Unregister [ConnectivityManager]
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        try {
            connectivityManager.unregisterNetworkCallback(heartBeatManager)
        } catch (e: Exception) {
            ToffeeAnalytics.logBreadCrumb("connectivity manager unregister error -> ${e.message}")
        }
    }
}