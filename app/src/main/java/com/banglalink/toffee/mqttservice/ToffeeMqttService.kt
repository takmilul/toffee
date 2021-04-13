package com.banglalink.toffee.mqttservice

import android.content.Context
import android.util.Log
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
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLSocketFactory

@Singleton
class ToffeeMqttService @Inject constructor(
    private val mPref: SessionPreference, 
    @ApplicationContext private val context: Context,
    private val shareCountRepository: ShareCountRepository,
    private val reactionStatusRepository: ReactionStatusRepository,
    private val subscriptionCountRepository: SubscriptionCountRepository,
) : MqttCallback, IMqttActionListener {
    
    private var gson: Gson? = null
    private var client: MqttAndroidClient? = null
    private val shareList = arrayListOf<ShareData>()
    private val reactionList = arrayListOf<ReactionData>()
    private val subscriptionList = arrayListOf<SubscriptionCountData>()
    
    fun initialize() {
        try {
            if (mPref.mqttHost.isNotBlank() && mPref.mqttClientId.isNotBlank() && mPref.mqttUserName.isNotBlank() && mPref.mqttPassword.isNotBlank()) {
                val host = EncryptionUtil.decryptResponse(mPref.mqttHost)
                val clientId = EncryptionUtil.decryptResponse(mPref.mqttClientId)
                val userName = EncryptionUtil.decryptResponse(mPref.mqttUserName)
                val password = EncryptionUtil.decryptResponse(mPref.mqttPassword)
                
                client = MqttAndroidClient(context, host, clientId).apply {
                    setCallback(this@ToffeeMqttService)
                    connect(getMqttConnectionOption(userName, password), null, this@ToffeeMqttService)
                }
                startScheduler()
            }
        }
        catch (e: Exception) {
            Log.e("MQTT_", "initialize: ${e.message}")
        }
    }
    
    fun startScheduler(){
        CoroutineScope(Default).launch {
            while (true)
                startDbBatchUpdate()
        }
    }
    
    private suspend fun startDbBatchUpdate() {
        delay(10_000)
        if (shareList.isNotEmpty()) {
            shareList.forEach {
                shareCountRepository.updateShareCount(it.contentId.toInt(), 1)
            }
            shareList.clear()
        }
        if (subscriptionList.isNotEmpty()) {
            subscriptionList.forEach {
                subscriptionCountRepository.updateSubscriptionCount(it.channelId, it.status)
            }
            subscriptionList.clear()
        }
        if (reactionList.isNotEmpty()) {
            reactionList.forEach {
                reactionStatusRepository.updateReaction(it.contentId, it.reactionType, it.reactionStatus)
            }
            reactionList.clear()
        }
    }
    
    private fun getMqttConnectionOption(userName: String, password: String): MqttConnectOptions {
        return MqttConnectOptions().apply {
            isCleanSession = false
            isAutomaticReconnect = true
            connectionTimeout = 30
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
            Log.e("MQTT_", "sendMessage: ${e.message}")
        }
    }
    
    override fun onSuccess(token: IMqttToken?) {
        if(token?.client?.isConnected == true && token.topics.isNullOrEmpty()) {
            Log.e("MQTT_", "onSuccess: Connected")
            val disconnectedBufferOptions = DisconnectedBufferOptions().apply {
                isBufferEnabled = true
                bufferSize = 100
                isPersistBuffer = false
                isDeleteOldestMessages = false
            }
            client?.setBufferOpts(disconnectedBufferOptions)
            client?.subscribe(REACTION_TOPIC, 2, null, this@ToffeeMqttService)
            client?.subscribe(SHARE_COUNT_TOPIC, 2, null, this@ToffeeMqttService)
            client?.subscribe(SUBSCRIPTION_TOPIC, 2, null, this@ToffeeMqttService)
        }
        else {
            gson = gson ?: Gson()
            Log.e("MQTT_", "onSuccess - subscribed: ${token?.topics}")
        }
    }
    
    override fun messageArrived(topic: String?, message: MqttMessage?) {
        if(message != null) {
            val jsonString = String(message.payload)
            
            try {
                when(topic) {
                    REACTION_TOPIC -> {
                        val data = gson!!.fromJson(jsonString, ReactionData::class.java)
                        if (data != null && data.customerId != mPref.customerId) {
                            reactionList.add(data)
                        }
                    }
                    SHARE_COUNT_TOPIC -> {
                        val data = gson!!.fromJson(jsonString, ShareData::class.java)
                        if (data != null) {
                            shareList.add(data)
                        }
                    }
                    SUBSCRIPTION_TOPIC -> {
                        val data = gson!!.fromJson(jsonString, SubscriptionCountData::class.java)
                        if (data != null && data.subscriberId != mPref.customerId){
                            subscriptionList.add(data)
                        }
                    }
                }
            }
            catch (e: Exception) {
                Log.e("MQTT_", "messageArrived: ${e.message}")
            }
        }
    }
    
    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Log.e("MQTT_", "deliveryComplete: $token")
    }
    
    override fun onFailure(token: IMqttToken?, error: Throwable?) {
        Log.e("MQTT_", "onFailure: ${error?.message}")
    }
    
    override fun connectionLost(error: Throwable?) {
        Log.e("MQTT_", "connectionLost: $error")
    }
    
    fun destroy() {
        client?.let { 
            it.unsubscribe(REACTION_TOPIC)
            it.unsubscribe(SHARE_COUNT_TOPIC)
            it.unsubscribe(SUBSCRIPTION_TOPIC)
            it.unregisterResources()
            it.disconnect()
        }
        client = null
        shareList.clear()
        reactionList.clear()
        subscriptionList.clear()
    }
}