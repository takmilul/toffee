package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckUpdateBean(
    @SerialName("updateAvailable")
    val updateAvailable: Int,
    @SerialName("message")
    val message: String,
    @SerialName("messageTitle")
    var messageTitle: String,
    @SerialName("decorationConfig")
    val decorationConfig : List<DecorationConfig>? = null
)
@Serializable
data class DecorationConfig (
    @SerialName("splashScreen")
    val splashScreen : List<DecorationData>?,
    @SerialName("topBar")
    val topBar : List<DecorationData>?,
    @SerialName("isFromCache")
    var isFromCache: Boolean = true
)
@Serializable
data class DecorationData (
    @SerialName("type")
    val type : String,
    @SerialName("is_active")
    val isActive : Int,
    @SerialName("image_path")
    val imagePathLight : String? = null,
    @SerialName("image_path_dark_mode")
    val imagePathDark : String? = null,
    @SerialName("start_date")
    val startDate : String,
    @SerialName("end_date")
    val endDate : String,
    @SerialName("duration")
    val duration : Int
)
