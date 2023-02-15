package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class CheckUpdateBean(
    @SerializedName("updateAvailable")
    val updateAvailable: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("messageTitle")
    var messageTitle: String,
    @SerializedName("decorationConfig")
    val decorationConfig : List<DecorationConfig>? = null
)

data class DecorationConfig (
    @SerializedName("splashScreen")
    val splashScreen : List<DecorationData>?,
    @SerializedName("topBar")
    val topBar : List<DecorationData>?,
    @SerializedName("isFromCache")
    var isFromCache: Boolean = true
)

data class DecorationData (
    @SerializedName("type")
    val type : String,
    @SerializedName("is_active")
    val isActive : Int,
    @SerializedName("image_path")
    val imagePathLight : String? = null,
    @SerializedName("image_path_dark_mode")
    val imagePathDark : String? = null,
    @SerializedName("start_date")
    val startDate : String,
    @SerializedName("end_date")
    val endDate : String,
    @SerializedName("duration")
    val duration : Int
)
