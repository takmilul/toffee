package com.banglalink.toffee.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerOverlayData(
    @SerialName("id")
    val id: Long = 0,
    @SerialName("function")
    val function: String? = null,
    @SerialName("content_id")
    val contentId: String? = null,
    @SerialName("timestamp")
    val timeStamp: String? = null,
    @SerialName("parameters")
    val params: OverlayParams? = null
)

@Serializable
data class OverlayParams(
    @SerialName("show")
    val displayParams: List<String>? = null,
    @SerialName("custom_text")
    val customText: String? = null,
    @SerialName("bg_color_code")
    val bgColorCode: String? = null,
    @SerialName("font_color_code")
    val fontColorCode: String? = null,
    @SerialName("font_size")
    val fontSize: String? = null,
    @SerialName("opacity")
    val opacity: String? = null,
    @SerialName("position")
    val position: String? = null, // constant/floating
    @SerialName("duration")
    val duration: Long = 0,
    @SerialName("from_position")
    val fromPosition: List<Float>? = listOf(0.0F, 0.0F),
    @SerialName("to_position")
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