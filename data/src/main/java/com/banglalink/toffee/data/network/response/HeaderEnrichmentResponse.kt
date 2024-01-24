package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HeaderEnrichmentResponse(
    @SerialName("msisdn")
    val phoneNumber: String? = null,
    @SerialName("is_bl")
    val isBanglalinkNumber: Boolean = false,
    @SerialName("lat")
    val lat: String? = null,
    @SerialName("lon")
    val lon: String? = null,
    @SerialName("user_ip")
    val userIp: String? = null,
    @SerialName("geo_city")
    val geoCity: String? = null,
    @SerialName("geo_location")
    val geoLocation: String? = null
) : BaseResponse()