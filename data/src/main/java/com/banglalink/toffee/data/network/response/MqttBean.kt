package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MqttBean(
    @SerialName("code")
    val code: Int,
    @SerialName("message")
    val message: String,
    @SerialName("mqttIsActive")
    val mqttIsActive: Int,
    @SerialName("mqttUrl")
    val mqttUrl: String,
    @SerialName("mqttUserId")
    val mqttUserId: String,
    @SerialName("mqttPassword")
    val mqttPassword: String
)