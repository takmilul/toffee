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
    val decorationConfig : DecorationConfig
    )

data class DecorationConfig (
    @SerializedName("splashScreen")
    val splashScreen : List<DecorationData>,
    @SerializedName("topBar")
    val topBar : List<DecorationData>
)

data class DecorationData (
    @SerializedName("type")
    val type : String,
    @SerializedName("is_active")
    val is_active : Int,
    @SerializedName("image_path")
    val image_path : String,
    @SerializedName("start_date")
    val start_date : String,
    @SerializedName("end_date")
    val end_date : String,
    @SerializedName("duration")
    val duration : Int
)
