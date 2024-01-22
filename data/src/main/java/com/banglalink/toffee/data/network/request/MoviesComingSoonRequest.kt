package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.apiservice.ApiNames
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviesComingSoonRequest(
    @SerialName("customerId")
    val customerId: Int,
    @SerialName("password")
    val password: String,
) : BaseRequest(ApiNames.GET_COMING_SOON_POSTER)