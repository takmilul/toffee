package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MyChannelPlaylist (
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("channel_creator_id")
    val channelCreatorId: Int,
    @SerializedName("is_channel_owner")
    val isOwner: Int,
    @SerializedName("channel_id")
    val channelId: Int,
    @SerializedName("playlist_table_id")
    val playlistTableId: Int,
    @SerializedName("content_id")
    val contentId: Int,
    @SerializedName("logo_web_url")
    val logoWebUrl: String?,
    @SerializedName("logo_mobile_url")
    val logoMobileUrl: String?,
    @SerializedName("logo_stb_url")
    val logoStbUrl: String?,
    @SerializedName("channel_logo")
    val channelLogo: String?,
    @SerializedName("totalContent")
    val totalContent: Int,
    @SerializedName("channel_name")
    val channelName: String?,
    @SerializedName("added_content_in_playlist")
    val playlistContentIdList: List<MyChannelPlaylistContentId>?,
    @SerializedName("create_time")
    val createTime: String? = null,
    @SerializedName("formattedCreateTime")
    var formattedCreateTime: String? = null,
    @SerializedName("landscape_ratio_1280_720")
    val landscape_ratio_1280_720: String? = null,
    @SerializedName("created_at")
    val created_at: String? = null,
)

data class MyChannelPlaylistContentId(
    @SerializedName("content_id")
    val contentId: String? = null,
)