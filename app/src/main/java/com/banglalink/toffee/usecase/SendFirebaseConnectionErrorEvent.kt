package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.FIREBASE_ERROR_TRACK_TOPIC
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendFirebaseConnectionErrorEvent @Inject constructor(
    private val preference: SessionPreference,
) {
    
    private val gson = Gson()
    
    suspend fun execute(sendToPubSub: Boolean = true) {
        PubSubMessageUtil.sendMessage(gson.toJson(FirebaseConnectionErrorData()), FIREBASE_ERROR_TRACK_TOPIC)
    }
}

data class FirebaseConnectionErrorData(
    val apiName: String = "",
    @SerializedName("phoneNumber")
    val phoneNo: String = SessionPreference.getInstance().phoneNumber,
): PubSubBaseRequest()
