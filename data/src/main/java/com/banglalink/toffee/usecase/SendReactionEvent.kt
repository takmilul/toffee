package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.mqttservice.ToffeeMqttService
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.REACTION_TOPIC
import com.banglalink.toffee.util.currentDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendReactionEvent @Inject constructor(
    private val mqttService: ToffeeMqttService
) {
    
    suspend fun execute(channelInfo: ChannelInfo, reactionInfo: ReactionInfo, reactionCount: Int, sendToPubSub:Boolean = true){
        if(sendToPubSub){
            sendToPubSub(channelInfo, reactionInfo, reactionCount)
        }
        else{
            sendToToffeeServer(reactionInfo, reactionCount)
        }
    }

    private fun sendToPubSub(channelInfo: ChannelInfo, reactionInfo: ReactionInfo, reactionCount: Int){
        val contentId = channelInfo.getContentId()
        val reactionData = ReactionData(
            id = reactionInfo.id?.toInt()?:0,
            customerId = reactionInfo.customerId,
            contentId = contentId.toLong(),
            reactionType = reactionInfo.reactionType,
            reactionStatus = reactionCount,
            reactionTime = reactionInfo.getReactionDate(),
        )
        PubSubMessageUtil.send(reactionData, REACTION_TOPIC)
        mqttService.send(reactionData, REACTION_TOPIC)
    }

    private suspend fun sendToToffeeServer(reactionInfo: ReactionInfo, reactionCount: Int){
        /*tryIO2 {
            toffeeApi.sendViewingContent(
                ViewingContentRequest(
                    contentType,
                    contentId,
                    preference.customerId,
                    preference.password,
                    preference.latitude,
                    preference.longitude
                )
            )
        }*/
    }
}

@Serializable
data class ReactionData(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("customer_id")
    val customerId: Int = 0,
    @SerialName("content_id")
    val contentId: Long = 0,
    @SerialName("reaction_type")
    val reactionType: Int = 0,
    @SerialName("reaction_status")
    val reactionStatus: Int = 0,
    @SerialName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerialName("reaction_time")
    val reactionTime: String? = null,
    @SerialName("reportingTime")
    val reportingTime: String = currentDateTime
)
