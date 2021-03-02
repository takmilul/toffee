package com.banglalink.toffee.usecase


import android.util.Log
import com.banglalink.toffee.data.database.dao.SubscriptionCountDao
import com.banglalink.toffee.data.database.entities.SubscriptionCount
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.banglalink.toffee.notification.SUBSCRIBER
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendSubscribeEvent @Inject constructor(
        private val preference: Preference,
        private val toffeeApi: ToffeeApi,
        private val subscriptionDao: SubscriptionCountDao
) {

    suspend fun execute(subscriptionICount: SubscriptionCount, subscriptionCount: Int, sendToPubSub:Boolean = true){
        if(sendToPubSub){
            sendToPubSub(subscriptionICount, subscriptionCount)
        }
        else{

            sendToPubSub(subscriptionICount,subscriptionCount)
        }
    }

    private fun sendToPubSub(SubscriptionCount: SubscriptionCount, subscriptionCount: Int){
        val subscriptionCountData = subscriptionCountData(
                channelId = SubscriptionCount.channelId,
                subscriberId = SubscriptionCount.subscriberId,
                status = SubscriptionCount.status,
                date_time=SubscriptionCount.getDate()
        )
        PubSubMessageUtil.sendMessage(Gson().toJson(subscriptionCountData), SUBSCRIBER)
        Log.e("Pubsub","data"+Gson().toJson(subscriptionCountData))
    }

    private suspend fun sendToToffeeServer(SubscriptionCount: SubscriptionCount, subscriptionCount: Int){
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

    private data class subscriptionCountData(
        @SerializedName("channel_id")
        val channelId: Int,
        @SerializedName("subscriber_id")
        val subscriberId: Int,
        @SerializedName("status")
        val status: Int,
        @SerializedName("date_time")
        val date_time: String
    )
}