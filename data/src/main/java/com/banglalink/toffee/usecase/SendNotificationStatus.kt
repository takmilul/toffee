package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.NOTIFICATION_TOPIC
import com.banglalink.toffee.notification.PUBSUBMessageStatus
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendNotificationStatus @Inject constructor() {
    suspend fun execute(notificationId: String?, messageStatus: PUBSUBMessageStatus, sendToPubSub: Boolean = true) {
        if (sendToPubSub) {
            PubSubMessageUtil.sendMessage(NotificationStatus(notificationId, messageStatus.ordinal), NOTIFICATION_TOPIC)
        }
    }
}

@Serializable
data class NotificationStatus(
    @SerialName("notificationId")
    val notificationId: String? = null,
    @SerialName("messageStatus")
    val messageStatus: Int = -1,
    @SerialName("userId")
    val userId: Int = SessionPreference.getInstance().customerId,
    @SerialName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
)