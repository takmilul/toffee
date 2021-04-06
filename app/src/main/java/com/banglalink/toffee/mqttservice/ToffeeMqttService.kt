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
            }
        }
        catch (e: Exception) {
            Log.e("MQTT_", "initialize: ${e.message}")
        }
    }
    
    private fun getMqttConnectionOption(userName: String, password: String): MqttConnectOptions {
        return MqttConnectOptions().apply {
            isCleanSession = false
            isAutomaticReconnect = true
            connectionTimeout = 3600
            socketFactory = SSLSocketFactory.getDefault()
            this.userName = userName
            this.password = password.toCharArray()
        }
    }
    
    fun sendMessage(jsonMessage: String, topic: String) {
        try {
            MqttMessage().apply { 
                payload = jsonMessage.toByteArray(charset("UTF-8"))
                client?.publish(topic, this)
            }
        }
        catch (e: Exception){
            
        }
    }
    
    override fun onSuccess(token: IMqttToken?) {
        if(token?.client?.isConnected == true && token.topics.isNullOrEmpty()) {
            val disconnectedBufferOptions = DisconnectedBufferOptions().apply {
                isBufferEnabled = true
                bufferSize = 100
                isPersistBuffer = false
                isDeleteOldestMessages = false
            }
            client?.setBufferOpts(disconnectedBufferOptions)
            client?.subscribe(REACTION_TOPIC, 0, null, this@ToffeeMqttService)
            client?.subscribe(SHARE_COUNT_TOPIC, 0, null, this@ToffeeMqttService)
            client?.subscribe(SUBSCRIPTION_TOPIC, 0, null, this@ToffeeMqttService)
        }
        else {
            gson = gson ?: Gson()
        }
    }
    
    override fun messageArrived(topic: String?, message: MqttMessage?) {
        message?.let {
            val jsonString = String(it.payload)
            
            when(topic) {
                REACTION_TOPIC -> {
                    val data = gson!!.fromJson(jsonString, ReactionData::class.java)
                    if (data.customerId != mPref.customerId) {
                        CoroutineScope(Default).launch {
                            reactionStatusRepository.updateReaction(data.contentId, data.reactionType, data.reactionStatus)
                        }
                    }
                }
                SHARE_COUNT_TOPIC -> {
                    val data = gson!!.fromJson(jsonString, ShareData::class.java)
                    if (data.customerId != mPref.customerId) {
                        CoroutineScope(Default).launch {
                            shareCountRepository.updateShareCount(data.contentId.toInt(), 1)
                        }
                    }
                }
                SUBSCRIPTION_TOPIC -> {
                    val data = gson!!.fromJson(jsonString, SubscriptionCountData::class.java)
                    if (data.subscriberId != mPref.customerId){
                        CoroutineScope(Default).launch {
                            subscriptionCountRepository.updateSubscriptionCount(data.channelId, data.status)
                        }
                    }
                }
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
            client = null
        }
    }
}