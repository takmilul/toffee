package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.HistoryContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.UserChannel
import com.banglalink.toffee.util.Utils

class EditChannel(private val preference: Preference, private val toffeeApi: ToffeeApi) {
    
    var mOffset: Int = 0
        private set
    private val limit = 10
    
    suspend fun execute(): UserChannel {
        val channelCategoryList = listOf("Movie","Natok","Music","Video")
        val subscriptionPriceList = listOf("৳50","৳100","৳150","৳200","৳250")
        
        val response = tryIO {
            toffeeApi.getHistoryContents(
                HistoryContentRequest(
                    preference.customerId,
                    preference.password,
                    mOffset,
                    limit
                )
            )
        }
    
        mOffset += response.response.count
        if (response.response.channels != null) {
            val channel = response.response.channels.first().apply {
                this.formatted_view_count = Utils.getFormattedViewsText(this.view_count)
                this.formattedDuration = Utils.discardZeroFromDuration(this.duration)
            }
            return UserChannel(channel.id, channel.poster_url_mobile, channel.poster_url_mobile, channel.program_name, channel.description, channelCategoryList, channelCategoryList[0], subscriptionPriceList, subscriptionPriceList[0])
        }
        return UserChannel("1", "", "", "National Geography", "this is a general description for user channel", channelCategoryList, channelCategoryList[0], subscriptionPriceList, subscriptionPriceList[0])
    }
    
    suspend fun saveChanges(){
        
    }
}