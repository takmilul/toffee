package com.banglalink.toffee.analytics

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.banglalink.toffee.apiservice.HeaderEnrichmentService
import com.banglalink.toffee.data.network.util.resultFromResponse
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.banglalink.toffee.usecase.HeaderEnrichmentLogData
import com.banglalink.toffee.usecase.SendHeaderEnrichmentLogEvent
import com.banglalink.toffee.usecase.SendHeartBeat
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.today
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartBeatManager @Inject constructor(
    private val mPref: SessionPreference,
    private val sendHeartBeat: SendHeartBeat,
    private var connectionWatcher: ConnectionWatcher,
    private val sendHeLogEvent: SendHeaderEnrichmentLogEvent,
    private val headerEnrichmentService: HeaderEnrichmentService
) : LifecycleObserver, ConnectivityManager.NetworkCallback() {
    
    private var contentId = 0;
    private var contentType = ""
    private var isAppForeGround = false
    private val coroutineScope2 = CoroutineScope(Main)
    private lateinit var coroutineScope :CoroutineScope
    private val _heartBeatEventLiveData = MutableLiveData<Boolean>()
    val heartBeatEventLiveData = _heartBeatEventLiveData.toLiveData()
    
    companion object {
        private const val INITIAL_DELAY = 0L
        private const val TIMER_PERIOD = 30000// 30 sec
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackGround() {
        isAppForeGround = false
        coroutineScope.cancel()
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeGround() {
        isAppForeGround = true
        coroutineScope = CoroutineScope(Default)
        coroutineScope.launch {
            sendHeartBeat(sendToPubSub = false)
            execute()
        }
        sendHeaderEnrichmentLog()
    }
    
    private fun sendHeaderEnrichmentLog() {
        try {
            if (mPref.heUpdateDate != today && connectionWatcher.isOverCellular) {
                coroutineScope.launch {
                    val response = resultFromResponse { headerEnrichmentService.execute() }
                    when(response) {
                        is Resource.Success -> {
                            val data = response.data
                            mPref.heUpdateDate = today
                            mPref.latitude = data.lat ?: ""
                            mPref.longitude = data.lon ?: ""
                            mPref.userIp = data.userIp ?: ""
                            mPref.geoCity = data.geoCity ?: ""
                            mPref.geoLocation = data.geoLocation ?: ""
                            mPref.hePhoneNumber = data.phoneNumber
                            mPref.isHeBanglalinkNumber = data.isBanglalinkNumber
                            try {
                                sendHeLogEvent.execute(HeaderEnrichmentLogData().also {
                                    it.phoneNumber = mPref.hePhoneNumber
                                    it.isBlNumber = mPref.isHeBanglalinkNumber.toString()
                                })
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        is Resource.Failure -> {
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
                getError(e)
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