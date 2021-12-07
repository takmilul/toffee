package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class HeaderEnrichmentResponse(
    @SerializedName("msisdn")
    val phoneNumber: String,
    @SerializedName("is_bl")
    val isBanglalinkNumber: Boolean,
    @SerializedName("lat")
    val lat: String? = null,
    @SerializedName("lon")
    val lon: String? = null,
    @SerializedName("user_ip")
    val userIp: String? = null,
    @SerializedName("geo_city")
    val geoCity: String? = null,
    @SerializedName("geo_location")
    val geoLocation: String? = null
) : BaseResponse()