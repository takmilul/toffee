package com.banglalink.toffee.notification

import android.content.Context
import android.util.Log
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
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
import kotlinx.coroutines.*

const val PROJECTID = "toffee-261507"
const val TOPIC_ID = "fcm-notification-response"
const val NOTIFICATION_TOPIC = "projects/$PROJECTID/topics/$TOPIC_ID"
const val HEARTBEAT_TOPIC = "projects/$PROJECTID/topics/current_viewers_heartbeat"
const val VIEWCONTENT_TOPIC = "projects/$PROJECTID/topics/current_viewers"
const val BANDWIDTH_TRACK_TOPIC = "projects/$PROJECTID/topics/player_bandwidth"
const val API_ERROR_TRACK_TOPIC = "projects/$PROJECTID/topics/api_error"
const val FIREBASE_ERROR_TRACK_TOPIC = "projects/$PROJECTID/topics/firebase_connection_error"
const val APP_LAUNCH_TOPIC = "projects/$PROJECTID/topics/app_launch"
const val LOGIN_LOG_TOPIC = "projects/$PROJECTID/topics/login_log"
const val REACTION_TOPIC = "projects/$PROJECTID/topics/ugc_reaction"
const val SHARE_COUNT_TOPIC = "projects/$PROJECTID/topics/share_count"
const val SUBSCRIPTION_TOPIC = "projects/$PROJECTID/topics/channels_subscribers"
const val CONTENT_REPORT_TOPIC = "projects/$PROJECTID/topics/report_inappropriate_content"
const val USER_INTEREST_TOPIC = "projects/$PROJECTID/topics/user_interest"
const val USER_OTP_TOPIC = "projects/$PROJECTID/topics/user_otp_log"

object PubSubMessageUtil {

    private lateinit var client:Pubsub
    private val TAG = "PubSubMessageUtil"
    private val coroutineContext = Dispatchers.IO + SupervisorJob()
    private val coroutineScope = CoroutineScope(coroutineContext)

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
            sendMessage(getPubSubMessage(notificationId, messageStatus), NOTIFICATION_TOPIC)
        }
    }

    fun sendMessage(jsonMessage: String, topic:String) {
         coroutineScope.launch {
             withContext(Dispatchers.IO){
                 try {
                     val batch = client.batch()
                     Log.d("PUBSUB - $topic",  jsonMessage)
                     val pubsubMessage = PubsubMessage()
                     pubsubMessage.encodeData(jsonMessage.toByteArray(charset("UTF-8")))
                     val publishRequest = PublishRequest()
                     publishRequest.messages = ImmutableList.of(
                         pubsubMessage
                     )
                client.projects().topics().publish(topic, publishRequest).queue(batch, callback)
                batch?.execute()

                } catch (ex: Exception) {
                    Log.e("PUBSUB - $topic", ex.message, ex)
                }
            } 
         }
    }

    var callback: JsonBatchCallback<PublishResponse?> =
        object : JsonBatchCallback<PublishResponse?>() {
            override fun onSuccess(t: PublishResponse?, responseHeaders: HttpHeaders?) {
                Log.d("PUBSUB", "published ! "+t?.messageIds)
            }
            override fun onFailure(e: GoogleJsonError, responseHeaders: HttpHeaders) {
                Log.e("PUBSUB", "error Message: " + e.message)
            }
        }

    private fun getPubSubMessage(notificationId: String?, messageStatus: PUBSUBMessageStatus): String {
        val jObj = JsonObject().apply { 
            addProperty("notificationId", notificationId)
            addProperty("userId", SessionPreference.getInstance().customerId)
            addProperty("messageStatus", messageStatus.ordinal)
            addProperty("device_id", CommonPreference.getInstance().deviceId)
        }
        Log.i(TAG, jObj.toString())
        return jObj.toString()
    }
}