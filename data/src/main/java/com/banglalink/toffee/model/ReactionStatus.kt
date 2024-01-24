package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class ReactionStatus(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("content_id")
    val content_id: Long = 0,
    @SerialName("like")
    var like: Long = 0,
    @SerialName("love")
    var love: Long = 0,
    @SerialName("haha")
    var haha: Long = 0,
    @SerialName("wow")
    var wow: Long = 0,
    @SerialName("sad")
    var sad: Long = 0,
    @SerialName("angry")
    var angry: Long = 0
): Parcelable