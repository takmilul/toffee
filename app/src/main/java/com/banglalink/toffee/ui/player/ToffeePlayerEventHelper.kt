package com.banglalink.toffee.ui.player

import android.os.Build
import com.banglalink.toffee.data.database.entities.PlayerEventData
import com.banglalink.toffee.data.repository.PlayerEventRepository
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.receiver.ConnectionWatcher
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.PingData
import com.banglalink.toffee.util.currentDateTimeMillis
import com.google.ads.interactivemedia.v3.api.Ad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToffeePlayerEventHelper @Inject constructor(
    private val cPref: CommonPreference,
    private val mPref: SessionPreference,
    private val connectionWatcher: ConnectionWatcher,
    private val playerEventRepository: PlayerEventRepository,
) {
    private var sequenceId: Int = 0
    private val eventMutex = Mutex()
    private var isContentPlaying = false
    private var schedulerJob: Job? = null
    private var playerEventData: PlayerEventData? = null
    private var coroutineScope = CoroutineScope(IO)
    
    init {
        if (mPref.isPlayerMonitoringActive) {
            val appLivecycleId = cPref.deviceId.plus("_").plus(System.nanoTime())
            playerEventData = PlayerEventData().apply {
                this.appLifeCycleId = appLivecycleId
            }
            appForegrounded("App started")
            startScheduler()
        }
    }
    
    fun startContentPlayingSession(contentId: String) {
        sequenceId = 0
        isContentPlaying = true
        val sessionId = contentId.plus("_").plus(System.nanoTime())
        playerEventData?.contentPlayingSessionId = sessionId
        setPlayerEvent("Content clicked for Playing")
    }
    
    fun startPlayerSession() {
        val sessionId = mPref.customerId.toString().plus("_").plus(System.nanoTime())
        playerEventData?.sessionId = sessionId
        setPlayerEvent("Player Opened")
    }
    
    private fun startScheduler(){
        schedulerJob = coroutineScope.launch(IO) {
            while (isActive) {
                sendPlayerEventData()
            }
        }
    }
    
    fun appForegrounded(event: String) {
        playerEventData?.isForeground = true.toString()
        setPlayerEvent(event)
    }
    
    fun appBackgrounded(event: String) {
        playerEventData?.isForeground = false.toString()
        setPlayerEvent(event)
    }
    
    fun setEventData(channelInfo: ChannelInfo, isDrmActive: Boolean, agent: String, url: String?, pingData: PingData?) {
        playerEventData?.apply {
            playingUrl = url
            this.agent = agent
            isDrm = isDrmActive
            channelInfo.let {
                contentId = it.id
                contentTitle = it.program_name
                contentCategoryId = it.categoryId
                contentCategoryName = it.category
                contentProviderId = it.content_provider_id
                contentProviderName = it.content_provider_name
                contentType = it.type
                contentDuration = it.duration
                seasonName = it.seriesName
                seasonNo = it.seasonNo
                episodeName = it.program_name
                episodeNo = it.episodeNo.toString()
            }
            setPingData(pingData)
        }
    }
    
    fun setPingData(pingData: PingData?) {
        playerEventData?.apply {
            pingData?.let {
                isInternetAvailable = if (Build.VERSION.SDK_INT != Build.VERSION_CODES.R) it.isOnline else null
                networkType = it.networkType
                ispOrTelecomOperator = it.ispOrTelecom
                remoteHost = it.host
                remoteIp = it.ip
                latencyClientToCdn = it.latency
            }
        }
    }
    
    fun setPlayerEvent(event: String, errorMessage: String? = null, errorCause: String? = null, errorCode: Int? = 200, eventId: Int? = 0) {
//        Log.i(PLAYER_EVENT_TAG, "Event: $event, Error Message: $errorMessage, Error Cause: $errorCause")
        addEventToDb(
            playerEventData?.apply {
                dateTime = currentDateTimeMillis
                playerEvent = event
                playerEventId = eventId
                this.errorMessage = errorMessage
                this.errorCause = errorCause
                this.statusCode = errorCode ?: 200
            }
        )
    }
    
    fun setAdData(ad: Ad?, eventName: String?, errorMessage: String? = null, isReset: Boolean = false) {
//        Log.i(PLAYER_EVENT_TAG, "Event: $eventName, Error Message: $errorMessage")
        Log.i("ADs_", "Event: $eventName, Error Message: $errorMessage")
        playerEventData?.apply {
            dateTime = currentDateTimeMillis
            adId = ad?.adId
            adCreativeId = ad?.creativeId
            adFirstCreativeId = ad?.adWrapperCreativeIds?.firstOrNull()?.toString()
            adFirstAdId = ad?.adWrapperIds?.firstOrNull()?.toString()
            adFirstAdSystem = ad?.adWrapperSystems?.firstOrNull()?.toString()
            adSystem = ad?.adWrapperSystems?.contentToString()
            adIsSlate = ad?.let { false.toString() }
            adTechnology = ad?.let { "Client Side" }
            adIsLive = ad?.isLinear?.toString()
            adPosition = ad?.adPodInfo?.podIndex?.let { if (it == 0) "PRE-ROLL" else if (it == - 1) "POST-ROLL" else "MID-ROLL" }
            adErrorMessage = errorMessage
            adEvent = eventName
        }
        if (!isReset) {
            val eventData = playerEventData?.copy()
            addEventToDb(eventData)
        }
    }
    
    private fun addEventToDb(playerEventData: PlayerEventData?) {
        playerEventData?.let {
            coroutineScope.launch {
                eventMutex.withLock {
                    playerEventRepository.insertAll(it.apply {
                        contentPlayingSessionSequenceId = if(isContentPlaying) (++sequenceId).toString() else null
                    })
                }
            }
        }
    }
    
    private suspend fun sendPlayerEventData(forceUpdate: Boolean = false) {
        if (!forceUpdate) {
            delay(15_000)
        }
        if (connectionWatcher.isOnline) {
            playerEventRepository.sendTopEventToPubSubAndRemove()
        }
    }
    
    fun endPlayerSession() {
        setPlayerEvent("player closed")
        playerEventData?.sessionId = null
    }
    
    fun endContentPlayingSession() {
        sequenceId = 0
        isContentPlaying = false
        playerEventData?.contentPlayingSessionId = null
    }
    
    fun release() {
        coroutineScope.launch {
            sendPlayerEventData(true)
        }
        schedulerJob?.cancel()
        schedulerJob = null
        coroutineScope.cancel()
    }
}