package com.banglalink.toffee.model

import com.google.gson.annotations.SerializedName

data class PlayerOverlayData(
    val id: Long,
    val function: String,

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

    val opacity: String,

    val position: String, // constant/floating

    val duration: Long,
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