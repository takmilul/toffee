package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.KABBIK_CURRENT_VIEWER
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendAudioBookViewContentEvent @Inject constructor() {
    
    fun execute(kabbikAudioBookLogData: KabbikAudioBookLogData) {
        PubSubMessageUtil.send(kabbikAudioBookLogData, KABBIK_CURRENT_VIEWER)
    }
}

data class KabbikAudioBookLogData(
    @SerializedName("content_id"     ) var contentId     : String? = null,
    @SerializedName("book_name"      ) var bookName      : String? = null,
    @SerializedName("book_category"  ) var bookCategory  : String? = null,
    @SerializedName("book_type"      ) var bookType      : String? = null,
    @SerializedName("lat"            ) var lat           : String? = null,
    @SerializedName("lon"            ) var lon           : String? = null,
) : PubSubBaseRequest()