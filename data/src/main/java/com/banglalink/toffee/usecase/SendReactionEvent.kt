package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.storage.CommonPreference
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.mqttservice.ToffeeMqttService
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.REACTION_TOPIC
import com.banglalink.toffee.util.currentDateTime
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendReactionEvent @Inject constructor(
    private val mqttService: ToffeeMqttService
) {

    private val gson = Gson()
    
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
        PubSubMessageUtil.sendMessage(gson.toJson(reactionData), REACTION_TOPIC)
        mqttService.sendMessage(gson.toJson(reactionData), REACTION_TOPIC)
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

data class ReactionData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("customer_id")
    val customerId: Int,
    @SerializedName("content_id")
    val contentId: Long,
    @SerializedName("reaction_type")
    val reactionType: Int,
    @SerializedName("reaction_status")
    val reactionStatus: Int,
    @SerializedName("device_id")
    val deviceId: String = CommonPreference.getInstance().deviceId,
    @SerializedName("reaction_time")
    val reactionTime: String,
    @SerializedName("reportingTime")
    val reportingTime: String = currentDateTime
)
