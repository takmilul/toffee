package com.banglalink.toffee.data.network.request

data class MqttRequest (
    val customerId:Int,
    val password:String,
): BaseRequest("mqttCredential")