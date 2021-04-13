package com.banglalink.toffee.data.network.response

data class MqttBean(
    val code: Int,
    val message: String,
    val mqttIsActive: Int,
    val mqttUrl: String,
    val mqttUserId: String,
    val mqttPassword: String
)