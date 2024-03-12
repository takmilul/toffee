package com.banglalink.toffee.notification

import android.content.Context
import android.util.Base64
import com.banglalink.toffee.Constants
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.di.NetworkModuleLib
import com.banglalink.toffee.util.Log
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.batch.json.JsonBatchCallback
import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.pubsub.Pubsub
import com.google.api.services.pubsub.PubsubScopes
import com.google.api.services.pubsub.model.PublishRequest
import com.google.api.services.pubsub.model.PublishResponse
import com.google.api.services.pubsub.model.PubsubMessage
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import timber.log.Timber

const val PROJECT_ID = "toffee-261507"
const val TOPIC_ID = "fcm-notification-response"
const val NOTIFICATION_TOPIC = "projects/$PROJECT_ID/topics/$TOPIC_ID"
const val HEARTBEAT_TOPIC = "projects/$PROJECT_ID/topics/current_viewers_heartbeat"
const val VIEW_CONTENT_TOPIC = "projects/$PROJECT_ID/topics/current_viewers"
const val BANDWIDTH_TRACK_TOPIC = "projects/$PROJECT_ID/topics/player_bandwidth"
const val API_ERROR_TRACK_TOPIC = "projects/$PROJECT_ID/topics/api_error"
const val FIREBASE_ERROR_TRACK_TOPIC = "projects/$PROJECT_ID/topics/firebase_connection_error"
const val APP_LAUNCH_TOPIC = "projects/$PROJECT_ID/topics/app_launch"
const val LOGIN_LOG_TOPIC = "projects/$PROJECT_ID/topics/login_log"
const val REACTION_TOPIC = "projects/$PROJECT_ID/topics/ugc_reaction"
const val SHARE_COUNT_TOPIC = "projects/$PROJECT_ID/topics/share_count"
const val SUBSCRIPTION_TOPIC = "projects/$PROJECT_ID/topics/channels_subscribers"
const val CONTENT_REPORT_TOPIC = "projects/$PROJECT_ID/topics/report_inappropriate_content"
const val USER_INTEREST_TOPIC = "projects/$PROJECT_ID/topics/user_interest"
const val USER_OTP_TOPIC = "projects/$PROJECT_ID/topics/user_otp_log"
const val HE_REPORT_TOPIC = "projects/$PROJECT_ID/topics/toffee_he_log"
const val DRM_UNAVAILABLE_TOPIC = "projects/$PROJECT_ID/topics/drm_unavailable_devices"
const val DRM_FALLBACK_TOPIC = "projects/$PROJECT_ID/topics/drm_fallback_log"
const val ADVERTISING_ID_TOPIC = "projects/$PROJECT_ID/topics/advertising_id_log"
const val PLAYER_EVENTS_TOPIC = "projects/$PROJECT_ID/topics/player-events"
const val CATEGORY_CHANNEL_SHARE_COUNT_TOPIC = "projects/$PROJECT_ID/topics/share_log"
const val FEATURE_PARTNER_LOG = "projects/$PROJECT_ID/topics/featured_partner_log"
const val PAYMENT_LOG_FROM_DEVICE = "projects/$PROJECT_ID/topics/payment_log_from_device"
const val KABBIK_CURRENT_VIEWER = "projects/$PROJECT_ID/topics/kabbik_current_viewer"
const val KABBIK_CURRENT_VIEWERS_HEARTBEAT = "projects/$PROJECT_ID/topics/kabbik_current_viewers_heartbeat"

const val LOGOUT_LOG_TOPIC = "projects/$PROJECT_ID/topics/toffee_app_logout"

object PubSubMessageUtil {
    lateinit var client: Pubsub
    private const val TAG = "PubSubMessageUtil"
    private val coroutineContext = IO + SupervisorJob()
    val coroutineScope = CoroutineScope(coroutineContext)
    val json = NetworkModuleLib.providesJsonWithConfig()
    
    fun init(context: Context) {
        val httpTransport = NetHttpTransport()//AndroidHttp.newCompatibleTransport()
        val json: JacksonFactory? = JacksonFactory.getDefaultInstance()
        val timeout = SessionPreference.getInstance().externalTimeOut
        val credential = GoogleCredential.fromStream(
            Base64.decode(Constants.GCP_CREDENTIAL, Base64.NO_WRAP).inputStream()
        ).setExpirationTimeMilliseconds(timeout * 1_000L).createScoped(PubsubScopes.all())
        val builder = Pubsub.Builder(httpTransport, json, credential).setApplicationName("PubSubClient")
        client = builder.build()
    }
    
    fun sendNotificationStatus(notificationId: String?, messageStatus: PUBSUBMessageStatus) {
        coroutineScope.launch {
            send(getPubSubMessage(notificationId, messageStatus), NOTIFICATION_TOPIC)
        }
    }
    
    fun sendMessage(jsonMessage: String, topic: String) {
        coroutineScope.launch {
            runCatching {
                val batch = client.batch()
                Log.i("PUBSUB - $topic", jsonMessage)
                val pubsubMessage = PubsubMessage()
                pubsubMessage.encodeData(jsonMessage.toByteArray(charset("UTF-8")))
                val publishRequest = PublishRequest()
                publishRequest.messages = listOf(pubsubMessage)
                client.projects().topics().publish(topic, publishRequest).queue(batch, callback)
                batch?.execute()
            }.onFailure {
                Log.e("PUBSUB - $topic", it.message, it)
            }
        }
    }
    
    inline fun <reified T: Any> send(data: T, topic: String) {
        coroutineScope.launch {
            runCatching {
                val batch = client.batch()
                val pubsubMessage = PubsubMessage()
                val prettyJson = json.encodeToString<T>(data)
                Timber.tag("PUBSUB - $topic").i(prettyJson)
                pubsubMessage.encodeData(prettyJson.toByteArray(charset("UTF-8")))
                val publishRequest = PublishRequest()
                publishRequest.messages = listOf(pubsubMessage)
                client.projects().topics().publish(topic, publishRequest).queue(batch, callback)
                batch?.execute()
            }.onFailure {
                Log.e("PUBSUB - $topic", it.message, it)
            }
        }
    }
    
    var callback: JsonBatchCallback<PublishResponse?> =
        object : JsonBatchCallback<PublishResponse?>() {
            override fun onSuccess(t: PublishResponse?, responseHeaders: HttpHeaders?) {
                Log.i("PUBSUB", "published ! " + t?.messageIds)
            }
            
            override fun onFailure(e: GoogleJsonError, responseHeaders: HttpHeaders) {
                Log.e("PUBSUB", "error Message: " + e.message)
            }
        }
    
    private fun getPubSubMessage(notificationId: String?, messageStatus: PUBSUBMessageStatus): JsonObject {
        val jObj = JsonObject().apply {
            addProperty("notificationId", notificationId)
            addProperty("userId", SessionPreference.getInstance().customerId)
            addProperty("messageStatus", messageStatus.ordinal)
            addProperty("device_id", CommonPreference.getInstance().deviceId)
        }
        Log.i(TAG, jObj.toString())
        return jObj
    }
}