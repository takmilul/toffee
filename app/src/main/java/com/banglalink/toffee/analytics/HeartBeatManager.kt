package com.banglalink.toffee.analytics

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.core.os.bundleOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.work.BackoffPolicy.LINEAR
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy.APPEND_OR_REPLACE
import androidx.work.NetworkType.CONNECTED
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest.Companion.MIN_BACKOFF_MILLIS
import com.banglalink.toffee.Constants.PLAYER_EVENT_TAG
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.HeaderEnrichmentService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.banglalink.toffee.ui.player.PlayerEventWorker
import com.banglalink.toffee.usecase.AdvertisingIdLogData
import com.banglalink.toffee.usecase.HeaderEnrichmentLogData
import com.banglalink.toffee.usecase.SendAdvertisingIdLogEvent
import com.banglalink.toffee.usecase.SendHeaderEnrichmentLogEvent
import com.banglalink.toffee.usecase.SendHeartBeat
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.SingleLiveEvent
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.today
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit.MILLISECONDS
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
    private val headerEnrichmentService: HeaderEnrichmentService,
) : DefaultLifecycleObserver, ConnectivityManager.NetworkCallback() {
    
    private var ownerId = 0
    private var contentId = 0
    private var dataSource = ""
    private var contentType = "0"
    private var isFirstLaunch = true
    private var isAppForeGround = false
    private var channelInfo: ChannelInfo? = null
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var coroutineScope3: CoroutineScope
    private val coroutineScope2 = CoroutineScope(Main)
    private val _heartBeatEventLiveData = MutableLiveData<Boolean>()
    val heartBeatEventLiveData = _heartBeatEventLiveData.toLiveData()
    private val _networkChangeEventLiveData = SingleLiveEvent<Boolean>()
    val networkChangeEventLiveData = _networkChangeEventLiveData.toLiveData()
    
    companion object {
        private const val INITIAL_DELAY = 0L
        private const val TIMER_PERIOD = 30000// 30 sec
    }
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isAppForeGround = true
        coroutineScope = CoroutineScope(Default)
        coroutineScope3 = CoroutineScope(IO + Job())
        coroutineScope.launch {
            sendHeartBeat(sendToPubSub = false)
            execute()
        }
        sendAdIdLog()
        sendHeaderEnrichmentLog()
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppForeGround = false
        coroutineScope.cancel()
        coroutineScope3.cancel()
        
        try {
            val constraints = Constraints.Builder().setRequiredNetworkType(CONNECTED).build()
            val workerRequest = OneTimeWorkRequestBuilder<PlayerEventWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(LINEAR, MIN_BACKOFF_MILLIS, MILLISECONDS)
                .build()
            WorkManager.getInstance(appContext).enqueueUniqueWork("sendPlayerEvent", APPEND_OR_REPLACE, workerRequest)
        } catch (e: Exception) {
            Log.i(PLAYER_EVENT_TAG, "release: worker error")
        }
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
            if (mPref.heUpdateDate != today && connectionWatcher.isOverCellular) {
                coroutineScope.launch {
                    val response = resultFromResponse { headerEnrichmentService.execute() }
                    when (response) {
                        is Resource.Success -> {
                            val data = response.data
                            mPref.heUpdateDate = today
                            try {
                                if (data.isBanglalinkNumber && !data.phoneNumber.isNullOrBlank()) {
                                    mPref.latitude = data.lat ?: ""
                                    mPref.longitude = data.lon ?: ""
                                    mPref.userIp = data.userIp ?: ""
                                    mPref.geoCity = data.geoCity ?: ""
                                    mPref.geoLocation = data.geoLocation ?: ""
                                    mPref.hePhoneNumber = data.phoneNumber!!
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
                                    "error_description" to response.error.msg
                                )
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
    
    private suspend fun execute() {
        delay(INITIAL_DELAY)
        if (isAppForeGround)
            sendHeartBeat()
        while (true) {
            delay(TIMER_PERIOD.toLong())
            if (isAppForeGround)
                sendHeartBeat()
        }
    }
    
    private suspend fun sendHeartBeat(isNetworkSwitch: Boolean = false, sendToPubSub: Boolean = true) {
        if (mPref.customerId != 0 && mPref.password.isNotBlank()) {
            try {
                if (channelInfo != null && channelInfo!!.isAudioBook) {
                    sendHeartBeat.executeKabbik(channelInfo!!)
                } else {
                    sendHeartBeat.execute(contentId, contentType, dataSource, ownerId, isNetworkSwitch, sendToPubSub)
                }
                _heartBeatEventLiveData.postValue(true)
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getError(e)
                
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.EXCEPTION,
                    bundleOf(
                        "api_name" to ApiNames.SEND_HEART_BEAT,
                        FirebaseParams.BROWSER_SCREEN to "Splash Screen",
                        "error_code" to error.code,
                        "error_description" to error.msg
                    )
                )
            }
        }
    }
    
    fun triggerEventViewingContentStart(
        playingContentId: Int,
        playingContentType: String,
        contentDataSource: String,
        channelOwnerId: Int,
    ) {
        contentId = playingContentId
        contentType = playingContentType
        dataSource = contentDataSource
        ownerId = channelOwnerId
    }
    
    fun triggerEventViewingContentStop() {
        contentId = 0
        ownerId = 0
        contentType = "0"
        dataSource = ""
        triggerEventViewingKabbikStop()
    }
    
    fun triggerEventViewingKabbikStart(channelInfo: ChannelInfo) {
        this.channelInfo = channelInfo
    }
    
    private fun triggerEventViewingKabbikStop() {
        channelInfo = null
    }
    
    override fun onAvailable(network: Network) {
        if (!coroutineScope2.isActive)
            return
        _networkChangeEventLiveData.postValue(true)
        coroutineScope2.launch {
            delay(1500)
            if (!isFirstLaunch) {
                sendHeartBeat(isNetworkSwitch = true, sendToPubSub = false)
            }
            isFirstLaunch = false
        }
    }
}