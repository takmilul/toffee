package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReactionStatus(
    val id: Long,
    val content_id: Long,
    var like: Long = 0,
    var love: Long = 0,
    var haha: Long = 0,
    var wow: Long = 0,
    var sad: Long = 0,
    var angry: Long = 0
): Parcelable