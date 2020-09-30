package com.banglalink.toffee.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Notification(
    val header: String,
    val title: String,
    val content: String?,
    val time: String
) : Parcelable 