package com.banglalink.toffee.data.network.request

import com.banglalink.toffee.model.PlayerSessionDetails
import com.banglalink.toffee.util.Utils
import com.google.gson.annotations.SerializedName

class PlayerSessionDetailsRequest(
    @SerializedName("playerSessionList")
    val playerSessionList: List<PlayerSessionDetails>
) :
    PubSubBaseRequest() {
    @SerializedName("initialTime")
    var initialTime = Utils.getDateTime()
}