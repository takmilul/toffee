package com.banglalink.toffee.data.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MqttBean(
    @SerialName("code")
    val code: Int = 0,
    @SerialName("message")
    val message: String? = null,
    @SerialName("mqttIsActive")
    val mqttIsActive: Int = 0,
    @SerialName("mqttUrl")
    val mqttUrl: String? = null,
    @SerialName("mqttUserId")
    val mqttUserId: String? = null,
    @SerialName("mqttPassword")
    val mqttPassword: String? = null
)