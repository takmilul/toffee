package com.banglalink.toffee.data.network.response

import com.google.gson.annotations.SerializedName

data class MqttBean(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("mqttIsActive")
    val mqttIsActive: Int,
    @SerializedName("mqttUrl")
    val mqttUrl: String,
    @SerializedName("mqttUserId")
    val mqttUserId: String,
    @SerializedName("mqttPassword")
    val mqttPassword: String
)