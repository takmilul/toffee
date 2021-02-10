package com.banglalink.toffee.notification

import android.content.Context
import android.util.Log
import com.banglalink.toffee.data.storage.Preference
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.http.HttpHeaders
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.pubsub.Pubsub
import com.google.api.services.pubsub.PubsubScopes
import com.google.api.services.pubsub.model.PublishRequest
import com.google.api.services.pubsub.model.PublishResponse
import com.google.api.services.pubsub.model.PubsubMessage
import com.google.common.collect.ImmutableList
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object PubSubMessageUtil {

    private val TAG = "PubSubMessageUtil"
    private val coroutineContext = Dispatchers.IO + SupervisorJob()
    private val coroutineScope = CoroutineScope(coroutineContext)

    private val PROJECTID = "toffee-261507"
    private val TOPIC_ID = "fcm-notification-response"
    private val notificationTopic = "projects/$PROJECTID/topics/$TOPIC_ID"
    private val heartBeatTopic = "projects/$PROJECTID/topics/current_viewers_heartbeat"
    private val viewContentTopic = "projects/$PROJECTID/topics/current_viewers"
    private val reactionTopic = "projects/$PROJECTID/topics/ugc_reaction"

    private lateinit var client:Pubsub

    fun init(context: Context){
        val httpTransport = AndroidHttp.newCompatibleTransport()
        val json: JacksonFactory? = JacksonFactory.getDefaultInstance()
        val credential = GoogleCredential.fromStream(
            context.assets.open("toffee-261507-c7793c98cdfd.json")
        ).createScoped(PubsubScopes.all())
        val builder =
            Pubsub.Builder(httpTransport, json, credential).setApplicationName("PubSubClient")
        client = builder.build()
    }
    
    fun sendNotificationStatus(notificationId: String?, messageStatus: PUBSUBMessageStatus) {
        coroutineScope.launch {
            sendMessage(getPubSubMessage(notificationId, messageStatus), notificationTopic)
        }
    }

    fun sendHeartBeatToPubSub(jsonStringRequest:String){
        coroutineScope.launch {
            sendMessage(jsonStringRequest, heartBeatTopic)
        }
    }

    fun sendViewContentToPubSub(jsonStringRequest:String){
        coroutineScope.launch {
            sendMessage(jsonStringRequest, viewContentTopic)
        }
    }

    fun sendReactionToPubSub(jsonStringRequest:String){
        coroutineScope.launch {
            sendMessage(jsonStringRequest, reactionTopic)
        }
    }

     private fun sendMessage(message: String,topic:String) {
        try {
            val batch = client.batch()
            Log.d("PUBSUB",  message)
            val pubsubMessage = PubsubMessage()
            pubsubMessage.encodeData(message.toByteArray(charset("UTF-8")))
            val publishRequest = PublishRequest()
            publishRequest.messages = ImmutableList.of(
                pubsubMessage
            )

            client.projects().topics().publish(topic, publishRequest).queue(batch, callback)
            batch?.execute()

        } catch (ex: Exception) {
            Log.e(TAG, ex.message, ex)
        }
    }

    var callback: JsonBatchCallback<PublishResponse?> =
        object : JsonBatchCallback<PublishResponse?>() {
            override fun onSuccess(t: PublishResponse?, responseHeaders: HttpHeaders?) {
                Log.d("PUBSUB", "published !")
            }
            override fun onFailure(
                e: GoogleJsonError,
                responseHeaders: HttpHeaders
            ) {
                // ERROR!
                Log.d("PUBSUB", "error Message: " + e.message)
            }
        }

    private fun getPubSubMessage(notificationId: String?, messageStatus: PUBSUBMessageStatus): String {
        val jObj = JsonObject();
        jObj.addProperty("notificationId", notificationId);
        jObj.addProperty("userId", Preference.getInstance().customerId);
        jObj.addProperty("messageStatus", messageStatus.ordinal);

        Log.i(TAG, jObj.toString())
        return jObj.toString();
    }

}