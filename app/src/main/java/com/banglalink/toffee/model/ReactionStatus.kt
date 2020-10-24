package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReactionStatus(
    val id: Long,
    val content_id: Long,
    val like: Long = 0,
    val love: Long = 0,
    val haha: Long = 0,
    val wow: Long = 0,
    val sad: Long = 0,
    val angry: Long = 0
): Parcelable