package com.banglalink.toffee.usecase


import android.util.Log
import com.banglalink.toffee.data.storage.ViewCountDAO
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.getFormattedViewsText

class GetViewCount(private val viewCountDAO: ViewCountDAO) {

    suspend operator fun invoke(channelInfo: ChannelInfo):String{
        var channelViewCount:String? = ""
        channelViewCount = channelInfo.id?.let { channelId ->
            val viewCount = viewCountDAO.getViewCountByChannelId(channelId.toInt())
            Log.i("viewcount","channel view count from db is $viewCount")
            Log.i("viewcount","original view count is ${channelInfo.view_count}")
            viewCount?.let {
                getFormattedViewsText(it.toString())
            }
        }
        return channelViewCount?: getFormattedViewsText(channelInfo.view_count)
    }
}