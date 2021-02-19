package com.banglalink.toffee.apiservice

import com.banglalink.toffee.common.paging.BaseApiService
import com.banglalink.toffee.data.network.request.ContentRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO2
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.model.Notification
import javax.inject.Inject

class GetNotifications @Inject constructor(private val preference: Preference, private val toffeeApi: ToffeeApi) : BaseApiService<Notification> {
    
    override suspend fun loadData(offset: Int, limit: Int): List<Notification> {
        val response = tryIO2 {
            toffeeApi.getContents(
                "VOD",
                0, 0,
                offset, 30,
                preference.getDBVersionByApiName("getUgcContentsV5"),
                ContentRequest(
                    0,
                    0,
                    "VOD",
                    preference.customerId,
                    preference.password,
                    offset = offset,
                    limit = limit
                )
            )
        }
        if (response.response.channels != null) {
            /*return response.response.channels.map {
                it.formatted_view_count = getFormattedViewsText(it.view_count)
                it.formattedDuration = discardZeroFromDuration(it.duration)
                it
            }*/
            val notifications: MutableList<Notification> = mutableListOf()
            repeat(limit) {
                notifications.add(Notification("Live Now", "Somebody That I Used To Know (feat. Kimbra) official music video...", null, "23m"))
                notifications.add(Notification("Uploaded Video", "Somebody That I Used To Know (feat. Kimbra) official music video...", null, "43m"))
                notifications.add(Notification("Commented on your video", "Somebody That I Used To Know (feat. Kimbra) official music video...", null, "56m"))
                notifications.add(Notification("Replied to your comment", "Somebody That I Used To Know (feat. Kimbra) official music video...","Hello! This is the expanded view of notification list items. Lorem Ipsum is simply dummy text of theprin and typesetting industry. Lorem Ipsum has been the industry's standard. dummy text ever since the 1500s",  "56m"))
            }
            return notifications
        }
        return listOf()
    }
}