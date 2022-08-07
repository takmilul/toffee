package com.banglalink.toffee.mqttservice

import android.content.Context
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.entities.ReactionStatusItem
import com.banglalink.toffee.data.database.entities.ShareCount
import com.banglalink.toffee.data.database.entities.SubscriptionCount
import com.banglalink.toffee.data.repository.ReactionStatusRepository
import com.banglalink.toffee.data.repository.ShareCountRepository
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.REACTION_TOPIC
import com.banglalink.toffee.notification.SHARE_COUNT_TOPIC
import com.banglalink.toffee.notification.SUBSCRIPTION_TOPIC
import com.banglalink.toffee.usecase.ReactionData
import com.banglalink.toffee.usecase.ShareData
import com.banglalink.toffee.usecase.SubscriptionCountData
import com.banglalink.toffee.util.EncryptionUtil
import com.banglalink.toffee.util.Log
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLSocketFactory
import kotlin.system.measureTimeMillis

@Singleton
class ToffeeMqttService @Inject constructor(
    private val mPref: SessionPreference, 
    @ApplicationContext private val context: Context,
    private val shareCountRepository: ShareCountRepository,
    private val reactionStatusRepository: ReactionStatusRepository,
    private val subscriptionCountRepository: SubscriptionCountRepository,
) : MqttCallback, IMqttActionListener {
    private var gson: Gson? = null
    private val shareListMutex = Mutex()
    private var schedulerJob: Job? = null
    private val reactionListMutex = Mutex()
    private val subscribeListMutex = Mutex()
    private var client: MqttAndroidClient? = null
    private val shareStatusList = arrayListOf<ShareCount>()
    private val reactionStatusList = arrayListOf<ReactionStatusItem>()
    private val subscriptionStatusList = arrayListOf<SubscriptionCount>()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    fun initialize() {
        try {
            if (mPref.mqttHost.isNotBlank() && mPref.mqttClientId.isNotBlank() && mPref.mqttUserName.isNotBlank() && mPref.mqttPassword.isNotBlank() && client == null) {
                val host = EncryptionUtil.decryptResponse(mPref.mqttHost)
                val clientId = EncryptionUtil.decryptResponse(mPref.mqttClientId)
                val userName = EncryptionUtil.decryptResponse(mPref.mqttUserName)
                val password = EncryptionUtil.decryptResponse(mPref.mqttPassword)
//                Log.i("MQ_", "host: $host, client_id: $clientId, user_name: $userName password: $password")
                ToffeeAnalytics.logBreadCrumb("creating mqtt because null")
                client = MqttAndroidClient(context, host, clientId).apply {
                    setCallback(this@ToffeeMqttService)
                    connect(getMqttConnectionOption(userName, password), null, this@ToffeeMqttService)
                    Log.i("MQ_", "initialize: connecting...")
                }
//                repeat(100){
//                    shareStatusList.add(ShareCount(it, 1))
//                    subscriptionStatusList.add(SubscriptionCount(it+50, 1))
//                    reactionStatusList.add(ReactionStatusItem(it+50, mPref.customerId, 1))
//                }
                
                startScheduler()
            }
        }
        catch (e: Exception) {
            ToffeeAnalytics.logBreadCrumb("exception when creating mqtt -> ${e.cause}")
            Log.e("MQ_", "initializationError: ${e.cause}")
        }
    }
    
    private fun startScheduler(){
        schedulerJob = coroutineScope.launch {
            while (isActive) {
                startDbBatchUpdate()
            }
        }
    }
    
    private suspend fun startDbBatchUpdate() {
        delay(20_000)
        var time = measureTimeMillis { 
            shareListMutex.withLock {
                if (shareStatusList.isNotEmpty()) {
                    shareCountRepository.updateShareCount(shareStatusList)
                    shareStatusList.clear()
                }
            }
        }
//        Log.i("MQT_", "share end $time")

        time = measureTimeMillis { 
            subscribeListMutex.withLock {
                if (subscriptionStatusList.isNotEmpty()) {
                    subscriptionCountRepository.updateSubscriptionCount(subscriptionStatusList)
                    subscriptionStatusList.clear()
                }
            }
        }
//        Log.i("MQT_", "subs end $time")

        time = measureTimeMillis { 
            reactionListMutex.withLock {
                if (reactionStatusList.isNotEmpty()) {
                    reactionStatusRepository.updateReaction(reactionStatusList)
                    reactionStatusList.clear()
                }
            }
        }
//        Log.i("MQT_", "reaction ended $time")
    }
    
    private fun getMqttConnectionOption(userName: String, password: String): MqttConnectOptions {
        return MqttConnectOptions().apply {
            isCleanSession = false
            isAutomaticReconnect = false
            connectionTimeout = 30
            keepAliveInterval = 300
            socketFactory = SSLSocketFactory.getDefault()
            this.userName = userName
            this.password = password.toCharArray()
        }
    }
    
    fun sendMessage(jsonMessage: String, topic: String) {
        try {
            MqttMessage().apply { 
                payload = jsonMessage.toByteArray(charset("UTF-8"))
                client?.publish(topic, payload, 2, true)
            }
        }
        catch (e: Exception){
            Log.e("MQ_", "sendMessageError: ${e.cause}")
        }
    }
    
    override fun onSuccess(token: IMqttToken?) {
        try {
            if (client?.isConnected == true && token?.topics.isNullOrEmpty()) {
                Log.i("MQ_", "onSuccess: connected")
                ToffeeAnalytics.logBreadCrumb("mqtt connected")
                val disconnectedBufferOptions = DisconnectedBufferOptions().apply {
                    isBufferEnabled = true
                    bufferSize = 100
                    isPersistBuffer = false
                    isDeleteOldestMessages = false
                }
                client?.setBufferOpts(disconnectedBufferOptions)
                client?.subscribe(arrayOf(REACTION_TOPIC, SHARE_COUNT_TOPIC, SUBSCRIPTION_TOPIC), intArrayOf(2, 2, 2), null, this@ToffeeMqttService)
            } else {
                gson = gson ?: Gson()
//                Log.i("MQ_", "onSuccess - subscribed: ${token?.topics}")
            }
        } catch (e: Exception) {
            ToffeeAnalytics.logBreadCrumb("mqtt exception onsuccess -> ${e.cause}")
            Log.e("MQ_", "onSuccessError: ${e.cause}")
        }
    }
    
    override fun messageArrived(topic: String?, message: MqttMessage?) {
        if(message != null) {
            val jsonString = String(message.payload)
//            Log.i("MQ_", "messageArrived: $message")
            try {
                when(topic) {
                    REACTION_TOPIC -> {
                        val data = gson!!.fromJson(jsonString, ReactionData::class.java)
                        if (data != null && data.customerId != mPref.customerId) {
                            coroutineScope.launch {
                                reactionListMutex.withLock {
                                    reactionStatusList.add(ReactionStatusItem(data.contentId.toInt(), data.reactionType, data.reactionStatus.toLong()))
                                }
                            }
                        }
                    }
                    SHARE_COUNT_TOPIC -> {
                        val data = gson!!.fromJson(jsonString, ShareData::class.java)
                        if (data != null) {
                            coroutineScope.launch {
                                shareListMutex.withLock {
                                    shareStatusList.add(ShareCount(data.contentId.toInt(), 1))
                                }
                            }
                        }
                    }
                    SUBSCRIPTION_TOPIC -> {
                        val data = gson!!.fromJson(jsonString, SubscriptionCountData::class.java)
                        if (data != null && data.subscriberId != mPref.customerId){
                            coroutineScope.launch {
                                subscribeListMutex.withLock {
                                    subscriptionStatusList.add(SubscriptionCount(data.channelId, data.status.toLong()))
                                }
                            }
                        }
                    }
                }
            }
            catch (e: Exception) {
                Log.e("MQ_", "messageArrivedError: ${e.cause}")
            }
        }
    }
    
    override fun deliveryComplete(token: IMqttDeliveryToken?) {
//        Log.i("MQ_", "deliveryComplete: $token")
    }
    
    override fun onFailure(token: IMqttToken?, error: Throwable?) {
        Log.i("MQ_", "onFailure: ${error?.cause}")
        ToffeeAnalytics.logBreadCrumb("mqtt failure -> ${error?.cause}")
    }
    
    override fun connectionLost(error: Throwable?) {
        Log.i("MQ_", "connectionLost: $error")
        ToffeeAnalytics.logBreadCrumb("mqtt connection lost -> ${error?.cause}")
    }
    
    fun destroy() {
        try {
            client?.let {
                if (it.isConnected) {
                    it.unsubscribe(arrayOf(REACTION_TOPIC, SHARE_COUNT_TOPIC, SUBSCRIPTION_TOPIC))
                }
                it.unregisterResources()
                it.close()
                it.disconnect(0)
                schedulerJob?.cancel()
                schedulerJob = null
//                Log.e("MQ_", "destroyed")
                ToffeeAnalytics.logBreadCrumb("mqtt destroyed")
            }
            client = null
            coroutineScope.cancel()
        }
        catch (e: Exception) {
            Log.e("MQ_", "disconnectionError: $e")
            ToffeeAnalytics.logBreadCrumb("mqtt disconnection error -> ${e.cause}")
        }
    }
}