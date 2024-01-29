package com.banglalink.toffee.data.database.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.common.annotation.KeepName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@KeepName
@Parcelize
@Serializable
data class NotificationInfo(
    @SerialName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @SerialName("userId")
    @ColumnInfo(defaultValue = "0")
    val userId: Int = 0,
    @SerialName("notificationType")
    val notificationType: String? = null,
    @SerialName("notificationId")
    val notificationId: String? = null,
    @SerialName("topic")
    @ColumnInfo(defaultValue = "0")
    val topic: Int = 0,
    @SerialName("sender")
    @ColumnInfo(defaultValue = "0")
    val sender: Int = 0,
    @SerialName("title")
    val title: String? = null,
    @SerialName("content")
    val content: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("thumbnailUrl")
    val thumbnailUrl: String? = null,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("resourceUrl")
    val resourceUrl: String? = null,
    @SerialName("playNowUrl")
    val playNowUrl: String? = null,
    @SerialName("watchLaterUrl")
    val watchLaterUrl: String? = null,
    @SerialName("receiveTime")
    val receiveTime: Long = System.currentTimeMillis(),
    @SerialName("seenTime")
    var seenTime: Long? = null,
    @SerialName("isSeen")
    var isSeen: Boolean = false
) : Parcelable