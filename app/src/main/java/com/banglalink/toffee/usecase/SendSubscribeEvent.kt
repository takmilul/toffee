package com.banglalink.toffee.usecase


import com.banglalink.toffee.data.database.dao.SubscriptionInfoDao
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
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
        private val subscriptionDao: SubscriptionInfoDao
) {

    suspend fun execute(subscriptionInfo: SubscriptionInfo, subscriptionCount: Int, sendToPubSub:Boolean = true){
        if(sendToPubSub){
            sendToPubSub(subscriptionInfo, subscriptionCount)
        }
        else{

            sendToPubSub(subscriptionInfo,subscriptionCount)
        }
    }

    private fun sendToPubSub(SubscriptionInfo: SubscriptionInfo, subscriptionCount: Int){
        val subscriptionCountData = subscriptionCountData(
                channelId = SubscriptionInfo.channelId,
                subscriberId = SubscriptionInfo.subscriberId,
                status = SubscriptionInfo.status,
                date_time=SubscriptionInfo.getDate()
        )
        PubSubMessageUtil.sendMessage(Gson().toJson(subscriptionCountData), SUBSCRIBER)
    }

    private suspend fun sendToToffeeServer(SubscriptionInfo: SubscriptionInfo, subscriptionCount: Int){
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