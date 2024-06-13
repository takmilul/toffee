package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.model.PlayerSessionDetails
import com.banglalink.toffee.util.currentDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class PlayerSessionDetailsRequest(
    @SerialName("playerSessionList")
    val playerSessionList: List<PlayerSessionDetails>
) : PubSubBaseRequest() {
    @SerialName("initialTime")
    var initialTime = currentDateTime
}