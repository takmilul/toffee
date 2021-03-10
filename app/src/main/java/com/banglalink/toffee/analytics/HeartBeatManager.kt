package com.banglalink.toffee.analytics

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.usecase.SendHeartBeat
import com.banglalink.toffee.util.getError
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HeartBeatManager @Inject constructor(
    private val sendHeartBeat: SendHeartBeat
) : LifecycleObserver, ConnectivityManager.NetworkCallback() {
    
    private var INITIAL_DELAY = 0L

    private val _heartBeatEventLiveData = MutableLiveData<Boolean>()
    val heartBeatEventLiveData = _heartBeatEventLiveData.toLiveData()

    private var isAppForeGround = false

    private val coroutineContext = Dispatchers.Default
    private val coroutineContext2 = Dispatchers.Main

    private var contentId = 0;
    private var contentType = ""
    private lateinit var  coroutineScope :CoroutineScope
    private val coroutineScope2 = CoroutineScope(coroutineContext2)
    
    companion object {
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
        coroutineScope = CoroutineScope(coroutineContext)
        coroutineScope.launch {
            sendHeartBeat(sendToPubSub = false)
            execute()
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
        if(Preference.getInstance().customerId!=0){
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