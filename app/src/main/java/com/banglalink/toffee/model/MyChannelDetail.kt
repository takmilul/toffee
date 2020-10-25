package com.banglalink.toffee.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MyChannelDetail(
    val id: Long,
    @SerializedName("channel_name")
    val channelName: String?,
    @SerializedName("channel_desc")
    val description: String?,
    @SerializedName("profile_url")
    val profileUrl: String?,
    @SerializedName("banner_url")
    val bannerUrl: String?,
    @SerializedName("category_id")
    val categoryId: Long
) : Parcelable