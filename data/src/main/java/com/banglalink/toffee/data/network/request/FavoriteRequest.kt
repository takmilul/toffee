package com.banglalink.toffee.data.network.request

import com.google.gson.annotations.SerializedName

data class FavoriteRequest(
    @SerializedName("contentId")
    val contentId: Int,
    @SerializedName("isFavorite")
    val isFavorite: Int,
    @SerializedName("customerId")
    val customerId: Int,
    @SerializedName("password")
    val password: String
) : BaseRequest("setUgcFavorites")