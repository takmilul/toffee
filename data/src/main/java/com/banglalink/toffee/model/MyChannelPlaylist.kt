package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MyChannelPlaylist (
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String? = null,
    @SerialName("channel_creator_id")
    val channelCreatorId: Int = 0,
    @SerialName("is_channel_owner")
    val isChannelOwner: String? = "0",
    @SerialName("channel_id")
    val channelId: Int = 0,
    @SerialName("playlist_table_id")
    val playlistTableId: Int = 0,
    @SerialName("content_id")
    val contentId: Int = 0,
    @SerialName("logo_web_url")
    val logoWebUrl: String? = null,
    @SerialName("logo_mobile_url")
    val logoMobileUrl: String? = null,
    @SerialName("logo_stb_url")
    val logoStbUrl: String? = null,
    @SerialName("channel_logo")
    val channelLogo: String? = null,
    @SerialName("totalContent")
    val totalContent: Int = 0,
    @SerialName("channel_name")
    val channelName: String? = null,
    @SerialName("added_content_in_playlist")
    val playlistContentIdList: List<MyChannelPlaylistContentId>? = null,
    @SerialName("create_time")
    val createTime: String? = null,
    @SerialName("formattedCreateTime")
    var formattedCreateTime: String? = null,
    @SerialName("landscape_ratio_1280_720")
    val landscape_ratio_1280_720: String? = null,
    @SerialName("created_at")
    val created_at: String? = null,
    @SerialName("playlist_share_url")
    val playlistShareUrl: String? = null,
    @SerialName("is_approved")
    val isApproved: Int = 0
) {
    fun isOwner(): Boolean {
        return isChannelOwner == "1"
    }
}
@Serializable
data class MyChannelPlaylistContentId(
    @SerialName("content_id")
    val contentId: String? = null,
)