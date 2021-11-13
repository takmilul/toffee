package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReactionStatus(
    @SerializedName("id")
    val id: Long,
    @SerializedName("content_id")
    val content_id: Long,
    @SerializedName("like")
    var like: Long = 0,
    @SerializedName("love")
    var love: Long = 0,
    @SerializedName("haha")
    var haha: Long = 0,
    @SerializedName("wow")
    var wow: Long = 0,
    @SerializedName("sad")
    var sad: Long = 0,
    @SerializedName("angry")
    var angry: Long = 0
): Parcelable