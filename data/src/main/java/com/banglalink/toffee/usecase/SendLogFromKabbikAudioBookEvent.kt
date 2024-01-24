package com.banglalink.toffee.usecase

import com.banglalink.toffee.data.network.request.PubSubBaseRequest
import com.banglalink.toffee.notification.KABBIK
import com.banglalink.toffee.notification.PubSubMessageUtil
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class SendLogFromKabbikAudioBookEvent @Inject constructor() {
    private val gson = Gson()

    fun execute(kabbikAudioBookLogData: KabbikAudioBookLogData) {
        PubSubMessageUtil.sendMessage(gson.toJson(kabbikAudioBookLogData), KABBIK)
    }
}

data class KabbikAudioBookLogData(
    @SerializedName("book_name"      ) var bookName      : String? = null,
    @SerializedName("book_category"  ) var bookCategory  : String? = null,
    @SerializedName("book_type"      ) var bookType      : String? = null,
    @SerializedName("lat"            ) var lat           : String? = null,
    @SerializedName("lon"            ) var lon           : String? = null,
    @SerializedName("content_id"     ) var contentId     : String? = null,
) : PubSubBaseRequest()