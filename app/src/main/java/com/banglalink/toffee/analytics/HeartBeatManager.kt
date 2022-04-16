package com.banglalink.toffee.analytics

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.HeaderEnrichmentService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.banglalink.toffee.usecase.*
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.today
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartBeatManager @Inject constructor(
    private val mPref: SessionPreference,
    private val sendHeartBeat: SendHeartBeat,
    private var connectionWatcher: ConnectionWatcher,
    @ApplicationContext private val appContext: Context,
    private val sendAdIdLogEvent: SendAdvertisingIdLogEvent,
    private val sendHeLogEvent: SendHeaderEnrichmentLogEvent,
    private val headerEnrichmentService: HeaderEnrichmentService
) : DefaultLifecycleObserver, ConnectivityManager.NetworkCallback() {
    
    private var contentId = 0;
    private var contentType = ""
    private var isAppForeGround = false
    private lateinit var coroutineScope :CoroutineScope
    private lateinit var coroutineScope3 :CoroutineScope
    private val coroutineScope2 = CoroutineScope(Main)
    private val _heartBeatEventLiveData = MutableLiveData<Boolean>()
    val heartBeatEventLiveData = _heartBeatEventLiveData.toLiveData()
    
    companion object {
        private const val INITIAL_DELAY = 0L
        private const val TIMER_PERIOD = 30000// 30 sec
    }
    
    override fun onStart(owner: LifecycleOwner) {
        isAppForeGround = true
        coroutineScope = CoroutineScope(Default)
        coroutineScope3 = CoroutineScope(IO + Job())
        coroutineScope.launch {
            sendHeartBeat(sendToPubSub = false)
            execute()
        }
        sendAdIdLog()
        sendHeaderEnrichmentLog()
        super.onStart(owner)
    }
    
    override fun onStop(owner: LifecycleOwner) {
        isAppForeGround = false
        coroutineScope.cancel()
        coroutineScope3.cancel()
        super.onStop(owner)
    }
    
    private fun sendAdIdLog() {
        if (mPref.adIdUpdateDate != today) {
            coroutineScope3.launch {
                kotlin.runCatching {
                    val adId = AdvertisingIdClient.getAdvertisingIdInfo(appContext).id
                    adId?.let {
                        sendAdIdLogEvent.execute(AdvertisingIdLogData(adId).also {
                            it.phoneNumber = mPref.phoneNumber
                            it.isBlNumber = mPref.isBanglalinkNumber
                        })
                    }
                    mPref.heUpdateDate = today
                }
            }
        }
    }
    
    private fun sendHeaderEnrichmentLog() {
        try {
            val isCellular = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) true else connectionWatcher.isOverCellular
            if (mPref.heUpdateDate != today && isCellular) {
                coroutineScope.launch {
                    val response = resultFromResponse { headerEnrichmentService.execute() }
                    when(response) {
                        is Resource.Success -> {
                            val data = response.data
                            mPref.heUpdateDate = today
                            try {
                                if (data.isBanglalinkNumber && data.phoneNumber.isNotBlank()) {
                                    mPref.latitude = data.lat ?: ""
                                    mPref.longitude = data.lon ?: ""
                                    mPref.userIp = data.userIp ?: ""
                                    mPref.geoCity = data.geoCity ?: ""
                                    mPref.geoLocation = data.geoLocation ?: ""
                                    mPref.hePhoneNumber = data.phoneNumber
                                    mPref.isHeBanglalinkNumber = data.isBanglalinkNumber
                                    sendHeLogEvent.execute(HeaderEnrichmentLogData().also {
                                        it.phoneNumber = mPref.hePhoneNumber
                                        it.isBlNumber = mPref.isHeBanglalinkNumber.toString()
                                    })
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        is Resource.Failure -> {

                            ToffeeAnalytics.logEvent(
                                ToffeeEvents.EXCEPTION,
                                bundleOf(
                                    "api_name" to "Header Enrichment",
                                    FirebaseParams.BROWSER_SCREEN to "Splash Screen",
                                    "error_code" to response.error.code,
                                    "error_description" to response.error.msg)
                            )

                            mPref.hePhoneNumber = ""
                            mPref.isHeBanglalinkNumber = false
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun execute(){
        delay(INITIAL_DELAY)
        if(isAppForeGround)
            sendHeartBeat()
        while(true){
            delay(TIMER_PERIOD.toLong())
            if(isAppForeGround)
                sendHeartBeat()
        }
    }

    private suspend fun sendHeartBeat(isNetworkSwitch:Boolean = false,sendToPubSub:Boolean = true){
        if(mPref.customerId != 0){
            try{
                sendHeartBeat.execute(contentId, contentType,isNetworkSwitch,sendToPubSub)
                _heartBeatEventLiveData.postValue(true)
            }catch (e:Exception){
                e.printStackTrace()
                val error =getError(e)

                ToffeeAnalytics.logEvent(
                    ToffeeEvents.EXCEPTION,
                    bundleOf(
                        "api_name" to ApiNames.SEND_HEART_BEAT,
                        FirebaseParams.BROWSER_SCREEN to "Splash Screen",
                        "error_code" to error.code,
                        "error_description" to error.msg)
                )
            }
        }
    }

    fun triggerEventViewingContentStart(playingContentId: Int, playingContentType: String) {
        contentId = playingContentId
        contentType = playingContentType
    }

    fun triggerEventViewingContentStop() {
        contentId = 0
        contentType = ""
    }

    override fun onAvailable(network: Network) {
        if(!coroutineScope2.isActive)
            return
        coroutineScope2.launch {
            delay(1500)
            sendHeartBeat(isNetworkSwitch = true, sendToPubSub = false)
        }
    }
}