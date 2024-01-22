package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.FIREBASE_ERROR_TRACK_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendFirebaseConnectionErrorEvent @Inject constructor(
    private val json: Json,
    private val preference: SessionPreference,
) {
    
    suspend fun execute(sendToPubSub: Boolean = true) {
        PubSubMessageUtil.sendMessage(json.encodeToString(FirebaseConnectionErrorData()), FIREBASE_ERROR_TRACK_TOPIC)
    }
}

@Serializable
data class FirebaseConnectionErrorData(
    @SerialName("apiName")
    val apiName: String = "",
    @SerialName("phoneNumber")
    val phoneNo: String = SessionPreference.getInstance().phoneNumber,
): PubSubBaseRequest()
