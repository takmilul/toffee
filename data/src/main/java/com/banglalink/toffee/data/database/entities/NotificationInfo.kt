package com.banglalink.toffee.data.database.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class NotificationInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    val userId: Int = 0,
    val notificationType: String?,
    val notificationId: String?,
    val topic: Int,
    val sender: Int,
    val title: String?,
    val content: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val imageUrl: String? = null,
    val resourceUrl: String? = null,
    val playNowUrl: String? = null,
    val watchLaterUrl: String? = null,
    val receiveTime: Long = System.currentTimeMillis(),
    var seenTime: Long? = null,
    var isSeen: Boolean = false
) : Parcelable