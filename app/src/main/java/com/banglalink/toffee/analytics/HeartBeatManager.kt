package com.banglalink.toffee.analytics

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.banglalink.toffee.data.network.retrofit.RetrofitApiClient
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.usecase.SendHeartBeat
import com.banglalink.toffee.util.getError
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.coroutines.*

object HeartBeatManager : LifecycleObserver {


    private val TIMER_PERIOD = 30000
    private var INITIAL_DELAY = 0L


    private var isAppFroreGround = false
    private val coroutineContext = Dispatchers.Main

    private var contentId = 0;
    private var contentType = ""

    lateinit var coroutineScope: CoroutineScope
    private val sendHeartBeat by unsafeLazy {
        SendHeartBeat(Preference.getInstance(),RetrofitApiClient.toffeeApi)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackGround() {
        isAppFroreGround = false
        coroutineScope.cancel()
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeGround() {
        isAppFroreGround = true
        coroutineScope = CoroutineScope(coroutineContext)
        coroutineScope.launch {
            execute()
        }
    }

    private suspend fun execute(){
        delay(INITIAL_DELAY)
        sendHeartBeat()
        while(true){
            delay(TIMER_PERIOD.toLong())
            sendHeartBeat()

        }
    }

    private suspend fun sendHeartBeat(){
        if(isAppFroreGround && Preference.getInstance().customerId!=0){
            try{
                sendHeartBeat.execute(contentId, contentType)
            }catch (e:Exception){
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

}