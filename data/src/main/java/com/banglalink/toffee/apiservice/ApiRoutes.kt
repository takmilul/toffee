package com.banglalink.toffee.apiservice

import com.banglalink.toffee.Constants

object ApiRoutes {
    const val GET_MY_CHANNEL_DETAILS = "ugc-channel-details"
    const val GET_MY_CHANNEL_PLAYLISTS = "ugc-playlist-names"
    const val GET_MY_CHANNEL_VIDEOS = "ugc-channel-all-content"
    const val GET_MY_CHANNEL_PLAYLIST_VIDEOS = "ugc-content-by-playlist"
    const val GET_ALL_USER_CHANNEL = "ugc-all-user-channel"
    const val GET_TRENDING_CHANNELS = "ugc-popular-channel"
    const val GET_SUBSCRIBED_CHANNELS = "ugc-channel-subscription-list"
    const val GET_USER_PLAYLISTS = "ugc-user-playlist-names"
    const val GET_USER_PLAYLIST_VIDEOS = "ugc-content-by-user-playlist"
    const val GET_HOME_FEED_VIDEOS = "ugc-contents-v5/${Constants.DEVICE_TYPE}/VOD/1/0/0" //baseUrl/contents-v5/deviceType/type/telcoId/categoryId/subCategoryId/limit/offset/dbVersion
}