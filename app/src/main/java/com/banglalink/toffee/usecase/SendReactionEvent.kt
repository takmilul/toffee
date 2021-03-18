package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.database.entities.ReactionInfo
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.REACTION_TOPIC
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendReactionEvent @Inject constructor(
    private val preference: SessionPreference,
    private val toffeeApi: ToffeeApi,
    private val reactionDao: ReactionDao
) {

    private val gson = Gson()
    
    suspend fun execute(reactionInfo: ReactionInfo, reactionCount: Int, sendToPubSub:Boolean = true){
        if(sendToPubSub){
            sendToPubSub(reactionInfo, reactionCount)
        }
        else{
            sendToToffeeServer(reactionInfo, reactionCount)
        }
    }

    private fun sendToPubSub(reactionInfo: ReactionInfo, reactionCount: Int){
        val reactionData = ReactionData(
            id = reactionInfo.id?.toInt()?:0,
            customerId = reactionInfo.customerId,
            contentId = reactionInfo.contentId,
            reactionType = reactionInfo.reactionType,
            reactionStatus = reactionCount,
            reactionTime = reactionInfo.getReactionDate(),
        )
        PubSubMessageUtil.sendMessage(gson.toJson(reactionData), REACTION_TOPIC)
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
    
    private data class ReactionData(
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
        @SerializedName("reaction_time")
        val reactionTime: String
    )
}