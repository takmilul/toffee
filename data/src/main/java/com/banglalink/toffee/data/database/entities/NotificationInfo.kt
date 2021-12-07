package com.banglalink.toffee.data.database.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.common.annotation.KeepName
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Entity
@KeepName
@Parcelize
data class NotificationInfo(
    @SerializedName("id")
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @SerializedName("userId")
    val userId: Int = 0,
    @SerializedName("notificationType")
    val notificationType: String?,
    @SerializedName("notificationId")
    val notificationId: String?,
    @SerializedName("topic")
    val topic: Int,
    @SerializedName("sender")
    val sender: Int,
    @SerializedName("title")
    val title: String?,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("resourceUrl")
    val resourceUrl: String? = null,
    @SerializedName("playNowUrl")
    val playNowUrl: String? = null,
    @SerializedName("watchLaterUrl")
    val watchLaterUrl: String? = null,
    @SerializedName("receiveTime")
    val receiveTime: Long = System.currentTimeMillis(),
    @SerializedName("seenTime")
    var seenTime: Long? = null,
    @SerializedName("isSeen")
    var isSeen: Boolean = false
) : Parcelable