package com.banglalink.toffee.data.network.request

data class FavoriteRequest(
    val contentId: Int,
    val isFavorite: Int,
    val customerId: Int,
    val password: String
):BaseRequest("setFavorites")