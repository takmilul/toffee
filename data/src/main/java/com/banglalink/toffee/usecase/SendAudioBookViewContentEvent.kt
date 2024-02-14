package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.KABBIK_CURRENT_VIEWER
import com.banglalink.toffee.notification.PubSubMessageUtil
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class SendAudioBookViewContentEvent @Inject constructor() {
    
    fun execute(kabbikAudioBookLogData: KabbikAudioBookLogData) {
        PubSubMessageUtil.send(kabbikAudioBookLogData, KABBIK_CURRENT_VIEWER)
    }
}

@Serializable
data class KabbikAudioBookLogData(
    @SerialName("content_id"     ) var contentId     : String? = null,
    @SerialName("book_name"      ) var bookName      : String? = null,
    @SerialName("book_category"  ) var bookCategory  : String? = null,
    @SerialName("book_type"      ) var bookType      : String? = null,
    @SerialName("lat"            ) var lat           : String? = null,
    @SerialName("lon"            ) var lon           : String? = null,
) : PubSubBaseRequest()