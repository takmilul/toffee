package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class MoviesContentVisibilityCards (
    @SerializedName("featuredContent")
    var featuredContent: Int = 0,
    @SerializedName("continueWatching")
    var continueWatching: Int = 0,
    @SerializedName("editorsChoice")
    var editorsChoice: Int = 0,
    @SerializedName("moviePreviews")
    var moviePreviews: Int = 0,
    @SerializedName("trendingNow")
    var trendingNow: Int = 0,
    @SerializedName("thriller")
    var thriller: Int = 0,
    @SerializedName("action")
    var action: Int = 0,
    @SerializedName("romantic")
    var romantic: Int = 0,
    @SerializedName("bangla")
    var bangla: Int = 0,
    @SerializedName("english")
    var english: Int = 0,
    @SerializedName("comingSoon")
    var comingSoon: Int = 0,
    @SerializedName("telefilm")
    var telefilm: Int = 0,
    @SerializedName("topMovieChannels")
    var topMovieChannels: Int = 0,
    @SerializedName("subCategory")
    var subCategory: Int = 0,
    @SerializedName("feed")
    var feed: Int = 0,
)