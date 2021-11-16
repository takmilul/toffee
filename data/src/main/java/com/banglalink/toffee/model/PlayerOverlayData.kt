package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class PlayerOverlayData(
    @SerializedName("id")
    val id: Long,
    @SerializedName("function")
    val function: String,
    @SerializedName("content_id")
    val contentId: String,
    @SerializedName("timestamp")
    val timeStamp: String,
    @SerializedName("parameters")
    val params: OverlayParams
)

data class OverlayParams(
    @SerializedName("show")
    val displayParams: List<String>,
    @SerializedName("custom_text")
    val customText: String,
    @SerializedName("bg_color_code")
    val bgColorCode: String,
    @SerializedName("font_color_code")
    val fontColorCode: String,
    @SerializedName("font_size")
    val fontSize: String,
    @SerializedName("opacity")
    val opacity: String,
    @SerializedName("position")
    val position: String, // constant/floating
    @SerializedName("duration")
    val duration: Long,
    @SerializedName("from_position")
    val fromPosition: List<Float>? = listOf(0.0F, 0.0F),
    @SerializedName("to_position")
    val toPosition: List<Float>? = listOf(1.0F, 0.0F),
)

/**
{
"id": 234,
"function": "notification_on_top_of_player",
"timestamp": "2021-03-08 ",
"parameters": {
"show": ["msisdn", "user_name", "device_id", "user_id", "device_type", "content_id", "public_ip", "location"],
"custom_text": "",
"bg_color_code": "#989908",
"font_color_code": "",
"font_size": "12px",
"opacity": "",
"position": "constant/floating",
"duration": 30
}
}
 *
 * */