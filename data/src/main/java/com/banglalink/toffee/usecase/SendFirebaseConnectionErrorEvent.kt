package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.FIREBASE_ERROR_TRACK_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendFirebaseConnectionErrorEvent @Inject constructor() {
    
    suspend fun execute(sendToPubSub: Boolean = true) {
        PubSubMessageUtil.sendMessage(FirebaseConnectionErrorData(), FIREBASE_ERROR_TRACK_TOPIC)
    }
}

@Serializable
data class FirebaseConnectionErrorData(
    @SerialName("apiName")
    val apiName: String = "",
    @SerialName("phoneNumber")
    val phoneNo: String = SessionPreference.getInstance().phoneNumber,
): PubSubBaseRequest()
