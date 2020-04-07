package com.banglalink.toffee.analytics

import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.toLiveData
import com.banglalink.toffee.usecase.SendHeartBeat
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.*

object HeartBeatManager : LifecycleObserver, ConnectivityManager.NetworkCallback() {


    private const val TIMER_PERIOD = 30000// 30 sec
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
    private val sendHeartBeat by unsafeLazy {
        SendHeartBeat(Preference.getInstance(),RetrofitApiClient.toffeeApi)
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

    private suspend fun sendHeartBeat(isNetworkSwitch:Boolean = false){
        if(Preference.getInstance().customerId!=0){
            try{
                sendHeartBeat.execute(contentId, contentType,isNetworkSwitch)
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
            sendHeartBeat(true)
        }
    }

}