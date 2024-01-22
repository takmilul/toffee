package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteContentRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
    @SerialName("offset")
    val offset: Int,
    @SerialName("limit")
    val limit: Int = 30
) : BaseRequest(ApiNames.GET_FAVORITE_CONTENTS)