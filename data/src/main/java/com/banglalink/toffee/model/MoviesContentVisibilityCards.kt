package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviesContentVisibilityCards (
    @SerialName("featuredContent")
    var featuredContent: Int = 0,
    @SerialName("continueWatching")
    var continueWatching: Int = 0,
    @SerialName("editorsChoice")
    var editorsChoice: Int = 0,
    @SerialName("moviePreviews")
    var moviePreviews: Int = 0,
    @SerialName("trendingNow")
    var trendingNow: Int = 0,
    @SerialName("thriller")
    var thriller: Int = 0,
    @SerialName("action")
    var action: Int = 0,
    @SerialName("romantic")
    var romantic: Int = 0,
    @SerialName("bangla")
    var bangla: Int = 0,
    @SerialName("english")
    var english: Int = 0,
    @SerialName("comingSoon")
    var comingSoon: Int = 0,
    @SerialName("telefilm")
    var telefilm: Int = 0,
    @SerialName("topMovieChannels")
    var topMovieChannels: Int = 0,
    @SerialName("subCategory")
    var subCategory: Int = 0,
    @SerialName("feed")
    var feed: Int = 0,
)