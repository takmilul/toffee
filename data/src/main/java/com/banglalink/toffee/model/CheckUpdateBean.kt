package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckUpdateBean(
    @SerialName("updateAvailable")
    val updateAvailable: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("messageTitle")
    var messageTitle: String? = null,
    @SerialName("decorationConfig")
    val decorationConfig : List<DecorationConfig>? = null
)
@Serializable
data class DecorationConfig (
    @SerialName("splashScreen")
    val splashScreen : List<DecorationData>? = null,
    @SerialName("topBar")
    val topBar : List<DecorationData>? = null,
    @SerialName("isFromCache")
    var isFromCache: Boolean = true
)
@Serializable
data class DecorationData (
    @SerialName("type")
    val type : String? = null,
    @SerialName("is_active")
    val isActive : Int = 0,
    @SerialName("image_path")
    val imagePathLight : String? = null,
    @SerialName("image_path_dark_mode")
    val imagePathDark : String? = null,
    @SerialName("start_date")
    val startDate : String? = null,
    @SerialName("end_date")
    val endDate : String? = null,
    @SerialName("duration")
    val duration : Int = 0
)
